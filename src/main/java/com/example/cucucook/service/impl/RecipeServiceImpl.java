package com.example.cucucook.service.impl;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.cucucook.common.ApiResponse;
import com.example.cucucook.common.CommonMethod;
import com.example.cucucook.common.FileUploadUtil;
import com.example.cucucook.domain.MemberRecipe;
import com.example.cucucook.domain.MemberRecipeImages;
import com.example.cucucook.domain.MemberRecipeIngredient;
import com.example.cucucook.domain.MemberRecipeProcess;
import com.example.cucucook.domain.PublicRecipe;
import com.example.cucucook.domain.RecipeCategory;
import com.example.cucucook.domain.RecipeComment;
import com.example.cucucook.domain.RecipeLike;
import com.example.cucucook.mapper.RecipeMapper;
import com.example.cucucook.service.RecipeService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

@Service
public class RecipeServiceImpl implements RecipeService {
  // 레시피 공공api가져오기
  @Value("${recipe.open.api.url}")
  private String openApiUrl;
  @Value("${recipe.open.api.key}")
  private String openApiKey;

  @Autowired
  private RecipeMapper recipeMapper;
  @Autowired
  private FileUploadUtil fileUploadUtil;

  /* 공공 api */
  // 회원 레시피 목록 외부API
  @Override
  public ApiResponse<List<PublicRecipe>> getPublicRecipeList(String search, int start, int display,
      String recipeCategoryId) {

    String message;
    boolean success = false;
    boolean hasMore = false;

    String result_json;
    String code;
    int end = start + display - 1;

    // api전송시 전체일시 빈값, 국&찌개 일 시 국 or 찌개로 파라미터 보내줘야함
    recipeCategoryId = "전체".equals(recipeCategoryId) ? ""
        : "국&찌개".equals(recipeCategoryId) ? "국" : recipeCategoryId;
    String rcpNm = "".equals(search) ? ""
        : "/RCP_NM=" + URLEncoder.encode(search, StandardCharsets.UTF_8);
    String rcpPat2 = "".equals(recipeCategoryId) ? ""
        : ("".equals(search) ? "/" : "&") + "RCP_PAT2="
            + URLEncoder.encode(recipeCategoryId, StandardCharsets.UTF_8);

    List<PublicRecipe> publicRecipeList = null;

    Map<String, Object> addDataMap = new HashMap<>();

    try {
      String urlString = openApiUrl + "/" + openApiKey + "/COOKRCP01/json/" + start + "/" + end + rcpNm
          + rcpPat2;
      URI uri = new URI(urlString);

      HttpClient client = HttpClient.newHttpClient();
      HttpRequest request = HttpRequest.newBuilder()
          .uri(uri)
          .header("Accept", "application/json")
          .GET()
          .build();

      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      result_json = response.body();

      boolean isJson = CommonMethod.isValidJson(result_json);

      if (!isJson) {
        message = result_json;
        success = false;
        return new ApiResponse<>(success, message, publicRecipeList, addDataMap);
      }

      Gson gson = new Gson();
      JsonElement jsonElement = gson.fromJson(result_json, JsonElement.class);

      JsonObject jsonObject = jsonElement.getAsJsonObject();
      JsonObject cookRcp01 = jsonObject.getAsJsonObject("COOKRCP01");
      JsonArray rowArray = cookRcp01.getAsJsonArray("row");
      JsonObject result = cookRcp01.getAsJsonObject("RESULT");
      Type recipeListType = new TypeToken<List<PublicRecipe>>() {
      }.getType();

      publicRecipeList = gson.fromJson(rowArray, recipeListType);

      message = result.get("MSG").getAsString();
      code = result.get("CODE").getAsString();
      success = "INFO-000".equals(code);

      if (publicRecipeList != null && publicRecipeList.size() == display)
        hasMore = true;

      addDataMap.put("hasMore", hasMore);

    } catch (InterruptedException | IOException | URISyntaxException e) {
      message = "E_ADMIN"; // 코드 잘못됐을때 보여줄 내용
      System.err.println("An error occurred: " + e.getMessage());

    }
    return new ApiResponse<>(success, message, publicRecipeList, addDataMap);
  }

  // 회원 레시피 목록 외부API
  @Override
  public ApiResponse<PublicRecipe> getPublicRecipe(String search, int start,
      int display) {

    String message;
    boolean success = false;

    String result_json;
    String code;
    String rcpNm = "".equals(search) ? ""
        : "/RCP_NM=" + URLEncoder.encode(search, StandardCharsets.UTF_8);

    PublicRecipe publicRecipe = null;
    Map<String, Object> addDataMap = new HashMap<>();

    try {
      String urlString = openApiUrl + "/" + openApiKey + "/COOKRCP01/json/" + start + "/" + display + rcpNm;
      URI uri = new URI(urlString);
      HttpClient client = HttpClient.newHttpClient();
      HttpRequest request = HttpRequest.newBuilder()
          .uri(uri)
          .header("Accept", "application/json")
          .GET()
          .build();

      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      result_json = response.body();

      boolean isJson = CommonMethod.isValidJson(result_json);

      if (!isJson) {
        message = result_json;
        return new ApiResponse<>(success, message, publicRecipe, addDataMap);
      }

      Gson gson = new Gson();
      JsonElement jsonElement = gson.fromJson(result_json, JsonElement.class);

      JsonObject jsonObject = jsonElement.getAsJsonObject();
      JsonObject cookRcp01 = jsonObject.getAsJsonObject("COOKRCP01");
      JsonArray rowArray = cookRcp01.getAsJsonArray("row");
      JsonObject result = cookRcp01.getAsJsonObject("RESULT");

      publicRecipe = gson.fromJson(rowArray.get(0), PublicRecipe.class);
      message = result.get("MSG").getAsString();
      code = result.get("CODE").getAsString();
      success = "INFO-000".equals(code);
    } catch (InterruptedException | IOException | URISyntaxException e) {
      message = "E_ADMIN"; // 코드 잘못됐을때 보여줄 내용
      System.err.println("An error occurred: " + e.getMessage());
    }
    return new ApiResponse<>(success, message, publicRecipe, addDataMap);
  }

  /* 회원 레시피 */
  // 회원 레시피 목록
  @Override
  public ApiResponse<List<MemberRecipe>> getMemberRecipeList(String search, String recipeCategoryId, int start,
      int display, String orderby, int memberId) {

    String message;
    String recipeId;
    boolean success = false;
    boolean hasMore = false;
    int memberRecipeTotalCnt;

    Map<String, Object> addDataMap = new HashMap<>();

    List<MemberRecipe> memberRecipeList = null;

    try {
      memberRecipeList = recipeMapper.getMemberRecipeList(search, recipeCategoryId, start, display,
          orderby);

      for (MemberRecipe memberRecipe : memberRecipeList) {
        boolean isMemberRecipeLike = false;
        recipeId = memberRecipe.getRecipeId();
        if (memberId > 0)
          isMemberRecipeLike = recipeMapper.isMemberRecipeLike(recipeId, memberId) > 0;
        memberRecipe.setMemberRecipeLike(isMemberRecipeLike);
      }

      memberRecipeTotalCnt = recipeMapper.getMemberRecipeCount(search, recipeCategoryId, orderby);
      message = (memberRecipeList == null || memberRecipeList.isEmpty()) ? "E_IS_DATA"
          : "S_IS_DATA";
      success = memberRecipeList != null && !memberRecipeList.isEmpty();
      if (memberRecipeList != null && memberRecipeList.size() == display)
        hasMore = true;
      addDataMap.put("hasMore", hasMore);
      addDataMap.put("totalCnt", memberRecipeTotalCnt);
    } catch (Exception e) {
      message = "E_ADMIN"; // 코드 잘못됐을때 보여줄 내용
      System.err.println("An error occurred: " + e.getMessage());
    }

    return new ApiResponse<>(success, message, memberRecipeList, addDataMap);
  }

  // 회원 레시피 상세보기
  // 레시피, 재료, 과정, 이미지, 레시피 찜수 다 포함되어야함
  @Override
  public ApiResponse<HashMap<String, Object>> getMemberRecipe(String recipeId, int memberId, boolean isUpdate) {

    String message;
    boolean success = false;

    HashMap<String, Object> memberRecipeHashMap = null;
    HashMap<String, Object> result = new HashMap<>();

    Map<String, Object> addDataMap = new HashMap<>();

    try {
      MemberRecipe memberRecipe = recipeMapper.getMemberRecipe(recipeId);

      boolean isMemberRecipeLike = false;
      if (memberId > 0)
        isMemberRecipeLike = recipeMapper.isMemberRecipeLike(recipeId, memberId) > 0;
      memberRecipe.setMemberRecipeLike(isMemberRecipeLike);
      result.put("memberRecipe", memberRecipe);

      // 재료 있으면 넣어주기
      List<MemberRecipeIngredient> memberRecipeIngredient = recipeMapper.getMemberRecipeIngredientList(recipeId);
      if (memberRecipeIngredient == null || memberRecipeIngredient.isEmpty()) {
        throw new Exception("E_IS_DATA");
      }
      result.put("memberRecipeIngredient", memberRecipeIngredient);

      // 과정 있으면 넣어주기
      List<MemberRecipeProcess> memberRecipeProcessList = recipeMapper.getMemberRecipeProcessList(recipeId);
      if (memberRecipeProcessList == null || memberRecipeProcessList.isEmpty()) {
        throw new Exception("E_IS_DATA");
      }

      result.put("memberRecipeProcessList", memberRecipeProcessList);

      memberRecipeHashMap = result;

      message = (memberRecipeHashMap.isEmpty()) ? "E_IS_DATA"
          : "S_IS_DATA";
      success = !memberRecipeHashMap.isEmpty();
      // 성공시 뷰카운트 업데이트 (수정페이지에서는 카운트가 올라가면안됨)
      if (success && !isUpdate) {
        recipeMapper.updateRecipeViewCount(recipeId);
      }
    } catch (Exception e) {
      message = "E_ADMIN"; // 코드 잘못됐을때 보여줄 내용
      System.err.println("An error occurred: " + e.getMessage());
    }

    return new ApiResponse<>(success, message, memberRecipeHashMap, addDataMap);
  }

  // 회원 레시피 글쓰기 레시피, 재료, 과정, 이미지 다 포함되어야함
  @Override
  @Transactional(rollbackFor = Exception.class)
  public ApiResponse<Integer> insertMemberRecipe(MemberRecipe memberRecipe,
      List<MemberRecipeIngredient> memberRecipeIngredientList,
      MultipartFile thumbnailImage, List<String> recipeProcessItemsContentsList,
      List<MultipartFile> recipeProcessItemsImageList)
      throws Exception {
    String message = "";
    boolean success = false;

    int insertResult = 0;
    String resultRecipeId;
    String resultImgId;

    Map<String, Object> addDataMap = new HashMap<>();

    try {
      // 1.레시피 기본정보 넣기
      recipeMapper.insertMemberRecipe(memberRecipe);
      resultRecipeId = memberRecipe.getRecipeId();

      // 2. 성공시 썸네일 파일 넣기 (파일 넣기 및 DB에 파일정보 넣어주기)
      if (thumbnailImage != null && thumbnailImage.getSize() > 0) {
        String serverFileName = fileUploadUtil.uploadFile(thumbnailImage, "recipe/" + resultRecipeId);

        resultImgId = insertMemberRecipeImages(thumbnailImage, serverFileName, resultRecipeId);

        recipeMapper.updateMemberRecipeImgId(resultRecipeId, resultImgId);
      } else {
        message = "E_IS_THUMBNAIL";
        throw new Exception(message);
      }

      // 3. 재료 넣기
      for (int i = 0; i < memberRecipeIngredientList.size(); i++) {
        MemberRecipeIngredient ingredient = memberRecipeIngredientList.get(i);
        ingredient.setOrderId(i); // orderId를 인덱스로 설정

        recipeMapper.insertMemberRecipeIngredient(resultRecipeId, ingredient);
      }

      // 4. 레시피 과정 넣기
      int validFileCount = 0;
      for (MultipartFile recipeProcessItemsImage : recipeProcessItemsImageList) {
        // 유효성 검사: null이 아니고, 빈 파일이 아닌지 확인
        if (recipeProcessItemsImage != null && recipeProcessItemsImage.getSize() > 0) {
          validFileCount++;
        }
      }
      if (validFileCount == 0) {
        message = "E_IS_RECIPE_IMG";
        throw new Exception(message);
      }

      for (int i = 0; i < recipeProcessItemsContentsList.size(); i++) {

        String serverFileName = fileUploadUtil.uploadFile(recipeProcessItemsImageList.get(i),
            "recipe/" + resultRecipeId);

        resultImgId = insertMemberRecipeImages(recipeProcessItemsImageList.get(i), serverFileName, resultRecipeId);

        MemberRecipeProcess process = new MemberRecipeProcess();
        process.setRecipeNumber(i + 1);
        process.setContents(recipeProcessItemsContentsList.get(i));
        process.setImgId(resultImgId);

        recipeMapper.insertMemberRecipeProcess(resultRecipeId, process);
      }

      // 최종적으로 모두 등록 완료한 경우 성공
      message = "S_ADD_DATA";
      success = true;
      insertResult = 1;

    } catch (Exception e) {
      message = "".equals(message) ? "E_ADD_DATA" : message;
      throw new Exception(message);

    }

    return new ApiResponse<>(success, message, insertResult, addDataMap);

  }

  // 회원 레시피 수정 레시피, 재료, 과정, 이미지 다 포함되어야함
  @Override
  @Transactional(rollbackFor = Exception.class)
  public ApiResponse<Integer> updateMemberRecipe(String recipeId, MemberRecipe memberRecipe,
      List<MemberRecipeIngredient> memberRecipeIngredientList,
      MultipartFile thumbnailImage, List<String> recipeProcessItemsImgIdList,
      List<String> recipeProcessItemsContentsList, List<MultipartFile> recipeProcessItemsImageList)
      throws Exception {
    String message = "";
    boolean success = false;

    int insertResult = 0;
    String resultImgId;

    Map<String, Object> addDataMap = new HashMap<>();

    try {
      // 1.레시피 기본정보 수정하기
      recipeId = memberRecipe.getRecipeId();
      recipeMapper.updateMemberRecipe(memberRecipe);

      // 2. 받아온 파일이 있다면 엎어쳐주기 아니면 해당 부분 넘어가기
      // 받아온 파일이 있을때 폼데이터로 넘어온 기존 서버 이미지값가지고 처리
      // 1) 해당 이미지값에 해당하는 이미지 서버 데이터 삭제
      // 2) 해당 이미지값에 해당하는 이미지테이블 db삭제
      // 3) 받아온 파일 처리 업로드랑 동일하게 처리
      if (thumbnailImage != null && thumbnailImage.getSize() > 0) {
        // 기존파일 삭제
        MemberRecipeImages memberRecipeImages = recipeMapper.getMemberRecipeImages(memberRecipe.getImgId());
        fileUploadUtil
            .deleteFile(memberRecipeImages.getServerImgPath() + "/" + memberRecipeImages.getServerImgName() + "."
                + memberRecipeImages.getExtension(), memberRecipeImages.getServerImgPath());
        recipeMapper.deleteMemberRecipeImages(memberRecipe.getImgId());

        // 새로운 파일 저장
        String serverFileName = fileUploadUtil.uploadFile(thumbnailImage, "recipe/" +
            recipeId);

        resultImgId = insertMemberRecipeImages(thumbnailImage, serverFileName, recipeId);

        recipeMapper.updateMemberRecipeImgId(recipeId, resultImgId);
      } else {
        if (memberRecipe.getImgId().isEmpty()) {
          message = "E_IS_THUMBNAIL";
          throw new Exception(message);
        }
      }

      // 3. 재료 넣기
      // 해당 레시피에 있는 재료 일괄 삭제 후 다시 insert
      recipeMapper.deleteMemberRecipeIngredient(recipeId, "");
      for (int i = 0; i < memberRecipeIngredientList.size(); i++) {
        MemberRecipeIngredient ingredient = memberRecipeIngredientList.get(i);
        ingredient.setOrderId(i); // orderId를 인덱스로 설정

        recipeMapper.insertMemberRecipeIngredient(recipeId, ingredient);
      }

      // 4. 레시피 과정 넣기 (이미지 수, 과정수 일치 하는지 체크)
      // 1) 해당 레시피에 있는 과정 일괄 삭제 후 다시 insert
      // 2) 받아온 이미지가 있으면 새로 넣어주고 기존 이미지 삭제
      // 3) 해당 이미지 값에 해당하는 이미지 테이블 db삭제
      // 4) 받아온 파일 처리 업로드랑 동일하게 처리
      recipeMapper.deleteMemberRecipeProcess(recipeId, "");
      int validFileCount = 0;
      if (recipeProcessItemsImgIdList.size() != recipeProcessItemsImageList.size())
        throw new Exception("E_IS_RECIPE_IMG");

      for (int i = 0; i < recipeProcessItemsImageList.size(); i++) {
        // 새로 업로드한 이미지가 있을 때
        if (recipeProcessItemsImageList.get(i) != null && recipeProcessItemsImageList.get(i).getSize() > 0) {
          validFileCount++;
        } else {
          if (recipeProcessItemsImgIdList.get(i).isEmpty())
            throw new Exception("E_IS_RECIPE_IMG");
          validFileCount++;
        }
      }

      if (validFileCount == 0) {
        message = "E_IS_RECIPE_IMG";
        throw new Exception(message);
      }

      for (int i = 0; i < recipeProcessItemsContentsList.size(); i++) {

        if (recipeProcessItemsImageList.get(i) != null && recipeProcessItemsImageList.get(i).getSize() > 0) {
          // 기존파일 삭제
          if (!"".equals(recipeProcessItemsImgIdList.get(i))) {
            MemberRecipeImages memberRecipeImages = recipeMapper
                .getMemberRecipeImages(recipeProcessItemsImgIdList.get(i));
            fileUploadUtil
                .deleteFile(memberRecipeImages.getServerImgPath() + "/" + memberRecipeImages.getServerImgName() + "."
                    + memberRecipeImages.getExtension(), memberRecipeImages.getServerImgPath());
            recipeMapper.deleteMemberRecipeImages(recipeProcessItemsImgIdList.get(i));
          }
          // 새로 업로드한 이미지 넣기
          String serverFileName = fileUploadUtil.uploadFile(recipeProcessItemsImageList.get(i),
              "recipe/" + recipeId);
          resultImgId = insertMemberRecipeImages(recipeProcessItemsImageList.get(i), serverFileName, recipeId);
        } else {
          // 새로 업로드한 이미지가 없을 때 현재 이미지 아이디 넣어줌
          resultImgId = recipeProcessItemsImgIdList.get(i);
        }

        MemberRecipeProcess process = new MemberRecipeProcess();
        process.setRecipeNumber(i + 1);
        process.setContents(recipeProcessItemsContentsList.get(i));
        process.setImgId(resultImgId);

        recipeMapper.insertMemberRecipeProcess(recipeId, process);
      }

      // 최종적으로 모두 등록 완료한 경우 성공
      message = "S_UPDATE_DATA";
      success = true;
      insertResult = 1;

    } catch (Exception e) {
      message = "".equals(message) ? "E_UPDATE_DATA" : message;
      throw new Exception(message); // Spring에 던져준다
    }

    return new ApiResponse<>(success, message, insertResult, addDataMap);

  }

  // 회원 레시피 삭제 레시피, 재료, 과정, 이미지, 댓글, 레시피 찜수 다 포함되어야함
  @Override
  @Transactional(rollbackFor = Exception.class)
  public ApiResponse<Integer> deleteMemberRecipe(String recipeId) throws Exception {

    String message;
    boolean success = false;
    int deleteResult = 0;

    Map<String, Object> addDataMap = new HashMap<>();

    try {
      // 1. 레시피 댓글 삭제
      recipeMapper.deleteRecipeCommentAll(recipeId);

      // 2. 레시피 찜수 삭제
      recipeMapper.deleteMemberRecipeLikeAll(recipeId);

      // 3. 레시피 재료삭제
      recipeMapper.deleteMemberRecipeIngredient(recipeId, "");

      // 4. 레시피 이미지파일,DB 삭제
      List<MemberRecipeImages> memberRecipeImagesList = recipeMapper.getMemberRecipeImagesList(recipeId);

      for (MemberRecipeImages memberRecipeImages : memberRecipeImagesList) {
        fileUploadUtil
            .deleteFile(memberRecipeImages.getServerImgPath() + "/" + memberRecipeImages.getServerImgName() + "."
                + memberRecipeImages.getExtension(), memberRecipeImages.getServerImgPath());
        recipeMapper.deleteMemberRecipeImages(memberRecipeImages.getImgId());
      }

      // 5. 레시피 과정삭제
      recipeMapper.deleteMemberRecipeProcess(recipeId, "");

      // 6. 레시피 삭제
      recipeMapper.deleteMemberRecipe(recipeId);

      // 최종적으로 모두 삭제 완료한 경우 성공
      success = true;
      message = "S_DEL_DATA";
      deleteResult = 1;
    } catch (IOException e) {
      message = "E_DEL_DATA"; // 코드 잘못됐을때 보여줄 내용
      System.err.println("An error occurred: " + e.getMessage());
      // e.printStackTrace(); // 오류 자세히 확인할때만 주석해제하고사용
      throw new Exception(message); // Spring에 던져준다
    } catch (Exception e) {
      message = "E_DEL_DATA"; // 코드 잘못됐을때 보여줄 내용
      System.err.println("An error occurred: " + e.getMessage());
      // e.printStackTrace(); // 오류 자세히 확인할때만 주석해제하고사용
      throw new Exception(message); // Spring에 던져준다
    }

    return new ApiResponse<>(success, message, deleteResult, addDataMap);

  }

  // 이미지 정보 DB저장
  public String insertMemberRecipeImages(MultipartFile imageFile, String serverFileName, String recipeId)
      throws Exception {

    MemberRecipeImages memberRecipeImage = new MemberRecipeImages();
    try {
      memberRecipeImage.setOrgImgName(fileUploadUtil.extractOriginalFileName(imageFile.getOriginalFilename()));
      memberRecipeImage.setServerImgName(serverFileName);
      memberRecipeImage.setExtension(fileUploadUtil.extractExtension(imageFile.getOriginalFilename(),
          imageFile.getContentType()));
      memberRecipeImage.setImgFileSize(Long.toString(imageFile.getSize()));
      memberRecipeImage.setServerImgPath(fileUploadUtil.getFileDir("recipe/" + recipeId));
      memberRecipeImage.setWebImgPath(fileUploadUtil.getFileWebDir("recipe/" + recipeId));
      recipeMapper.insertMemberRecipeImages(memberRecipeImage);
    } catch (Exception e) {
      throw new Exception("E_ADD_IMG_DATA"); // Spring에 던져준다
    }

    return memberRecipeImage.getImgId();

  }

  /* 레시피 댓글 */
  // 회원레시피 별 댓글목록
  @Override
  public ApiResponse<List<RecipeComment>> getRecipeCommentList(String recipeId, int start, int display) {

    String message;
    boolean success = false;
    int total_cnt;

    List<RecipeComment> recipeCommentList = null;

    Map<String, Object> addDataMap = new HashMap<>();

    try {
      recipeCommentList = recipeMapper.getRecipeCommentList(recipeId, start, display);
      total_cnt = recipeMapper.getRecipeCommentCount(recipeId);
      message = (recipeCommentList == null || recipeCommentList.isEmpty()) ? "E_IS_DATA"
          : "S_IS_DATA";
      success = recipeCommentList != null && !recipeCommentList.isEmpty();
      addDataMap.put("totalCnt", total_cnt);
    } catch (Exception e) {
      message = "E_ADMIN"; // 코드 잘못됐을때 보여줄 내용
      System.err.println("An error occurred: " + e.getMessage());
    }

    return new ApiResponse<>(success, message, recipeCommentList, addDataMap);
  }

  // 회원레시피 댓글
  @Override
  public ApiResponse<RecipeComment> getRecipeComment(String recipeId, String commentId) {

    String message;
    boolean success = false;

    RecipeComment recipeComment = null;

    Map<String, Object> addDataMap = new HashMap<>();

    try {
      recipeComment = recipeMapper.getRecipeComment(recipeId, commentId);
      message = (recipeComment == null) ? "E_IS_DATA"
          : "S_IS_DATA";
      success = recipeComment != null;
    } catch (Exception e) {
      message = "E_ADMIN"; // 코드 잘못됐을때 보여줄 내용
      System.err.println("An error occurred: " + e.getMessage());
    }

    return new ApiResponse<>(success, message, recipeComment, addDataMap);
  }

  // 회원레시피 댓글 쓰기
  @Override
  @Transactional(rollbackFor = Exception.class)
  public ApiResponse<Integer> insertRecipeComment(RecipeComment recipeComment) throws Exception {

    String message;
    boolean success = false;
    int insrtComment = 0;

    Map<String, Object> addDataMap = new HashMap<>();

    try {
      insrtComment = recipeMapper.insertRecipeComment(recipeComment);

      if (insrtComment > 0) {
        // 코멘트값이 수정 됐을 경우 계산 후 recipe에 값 update
        calcAvgCommentRate(recipeComment.getRecipeId());
        recipeMapper.updateRecipeCommentRate(recipeComment.getRecipeId(),
            calcAvgCommentRate(recipeComment.getRecipeId()));
      }

      message = (insrtComment > 0) ? "S_RECIPE_COMMENT_INSERT"
          : "E_RECIPE_COMMENT_INSERT";
      success = insrtComment > 0;
    } catch (Exception e) {
      message = "E_ADMIN"; // 코드 잘못됐을때 보여줄 내용
      throw new Exception(message); // Spring에 던져준다
    }

    return new ApiResponse<>(success, message, insrtComment, addDataMap);
  }

  // 회원레시피 댓글 수정
  @Override
  @Transactional(rollbackFor = Exception.class)
  public ApiResponse<Integer> updateRecipeComment(RecipeComment recipeComment) throws Exception {

    String message;
    boolean success = false;
    int updateComment = 0;
    Map<String, Object> addDataMap = new HashMap<>();

    try {
      updateComment = recipeMapper.updateRecipeComment(recipeComment);
      if (updateComment > 0) {
        // 코멘트값이 수정 됐을 경우 계산 후 recipe에 값 update
        calcAvgCommentRate(recipeComment.getRecipeId());
        recipeMapper.updateRecipeCommentRate(recipeComment.getRecipeId(),
            calcAvgCommentRate(recipeComment.getRecipeId()));
      }
      message = (updateComment > 0) ? "S_RECIPE_COMMENT_UPDATE"
          : "E_RECIPE_COMMENT_UPDATE";
      success = updateComment > 0;
    } catch (Exception e) {
      message = "E_ADMIN"; // 코드 잘못됐을때 보여줄 내용
      System.err.println("An error occurred: " + e.getMessage());
      throw new Exception(message); // Spring에 던져준다
    }

    return new ApiResponse<>(success, message, updateComment, addDataMap);
  }

  // 회원레시피 댓글 삭제
  @Override
  @Transactional(rollbackFor = Exception.class)
  public ApiResponse<Integer> deleteRecipeComment(String recipeId, String commentId) throws Exception {

    String message;
    boolean success = false;
    int deleteComment = 0;
    Map<String, Object> addDataMap = new HashMap<>();

    try {

      deleteComment = recipeMapper.deleteRecipeComment(recipeId, commentId);
      if (deleteComment > 0) {
        // 코멘트값이 수정 됐을 경우 계산 후 recipe에 값 update
        calcAvgCommentRate(recipeId);
        recipeMapper.updateRecipeCommentRate(recipeId, calcAvgCommentRate(recipeId));
      }
      message = (deleteComment > 0) ? "S_RECIPE_COMMENT_DELETE"
          : "E_RECIPE_COMMENT_DELETE";
      success = deleteComment > 0;
    } catch (Exception e) {
      message = "E_ADMIN"; // 코드 잘못됐을때 보여줄 내용
      System.err.println("An error occurred: " + e.getMessage());
      throw new Exception(message); // Spring에 던져준다
    }

    return new ApiResponse<>(success, message, deleteComment, addDataMap);
  }

  // 회원레시피 댓글 삭제(본댓글에 대댓글이 남겨져 있는 경우)
  @Override
  @Transactional(rollbackFor = Exception.class)
  public ApiResponse<Integer> deleteRecipeCommentHasChild(String recipeId, String commentId) throws Exception {

    String message;
    boolean success = false;
    int deleteComment = 0;

    Map<String, Object> addDataMap = new HashMap<>();

    try {
      deleteComment = recipeMapper.deleteRecipeCommentHasChild(recipeId, commentId);

      if (deleteComment > 0) {
        // 코멘트값이 수정 됐을 경우 계산 후 recipe에 값 update
        calcAvgCommentRate(recipeId);
        recipeMapper.updateRecipeCommentRate(recipeId, calcAvgCommentRate(recipeId));
      }
      message = (deleteComment > 0) ? "S_RECIPE_COMMENT_DELETE"
          : "E_RECIPE_COMMENT_DELETE";
      success = deleteComment > 0;
    } catch (Exception e) {
      message = "E_ADMIN"; // 코드 잘못됐을때 보여줄 내용
      System.err.println("An error occurred: " + e.getMessage());
      throw new Exception(message); // Spring에 던져준다
    }

    return new ApiResponse<>(success, message, deleteComment, addDataMap);
  }

  // 댓글 평점 계산
  public String calcAvgCommentRate(String recipeId) {

    double commentAvg = 0;
    int[] getRecipeCommentRateList = recipeMapper.getRecipeCommentRateList(recipeId);

    if (getRecipeCommentRateList.length > 0) {
      double sum = 0;
      for (int number : getRecipeCommentRateList) {
        sum += number; // 합계 계산
      }

      commentAvg = sum / getRecipeCommentRateList.length;
    }
    // 반올림
    BigDecimal roundedAverage = BigDecimal.valueOf(commentAvg)
        .setScale(2, RoundingMode.HALF_UP);
    // 소수점 0 제거
    String formattedAverage = roundedAverage.stripTrailingZeros().toPlainString();

    return formattedAverage;
  }

  /* 레시피 카테고리 */
  // 레시피 카테고리 목록(카운트별 레시피 카운트수 포함)
  @Override
  public ApiResponse<List<RecipeCategory>> getRecipeCategoryListWithMemberRecipeCount(
      @Param("search") String search) {

    String message;
    boolean success = false;

    List<RecipeCategory> getRecipeCategoryListWithMemberRecipeCount = null;

    Map<String, Object> addDataMap = new HashMap<>();

    try {
      getRecipeCategoryListWithMemberRecipeCount = recipeMapper
          .getRecipeCategoryListWithMemberRecipeCount(search);
      message = (getRecipeCategoryListWithMemberRecipeCount == null
          || getRecipeCategoryListWithMemberRecipeCount.isEmpty()) ? "E_IS_DATA"
              : "R_IS_DATA";
      success = getRecipeCategoryListWithMemberRecipeCount != null
          && !getRecipeCategoryListWithMemberRecipeCount.isEmpty();
    } catch (Exception e) {
      message = "E_ADMIN"; // 코드 잘못됐을때 보여줄 내용
      System.err.println("An error occurred: " + e.getMessage());
    }

    return new ApiResponse<>(success, message, getRecipeCategoryListWithMemberRecipeCount, addDataMap);
  }

  // 레시피 카테고리 목록(레시피 작성을 위함)
  @Override
  public ApiResponse<HashMap<String, Object>> getRecipeCategoryListForWrite() {

    String message;
    boolean success = false;
    HashMap<String, Object> categoryListForWirte = null;
    Map<String, Object> addDataMap = new HashMap<>();

    try {
      List<RecipeCategory> allCategoryList = recipeMapper.getRecipeCategoryList(0, 0, "", "");
      List<RecipeCategory> recipeCategoryList = allCategoryList.stream()
          .filter(category -> category.getDivision().equals("C")).collect(Collectors.toList());
      List<RecipeCategory> recipeMethodList = allCategoryList.stream()
          .filter(category -> category.getDivision().equals("M")).collect(Collectors.toList());
      List<RecipeCategory> recipeLevelList = allCategoryList.stream()
          .filter(category -> category.getDivision().equals("L")).collect(Collectors.toList());

      categoryListForWirte = new HashMap<>();
      categoryListForWirte.put("categoryList", recipeCategoryList);
      categoryListForWirte.put("methodList", recipeMethodList);
      categoryListForWirte.put("levelList", recipeLevelList);

      message = allCategoryList.isEmpty() ? "E_IS_DATA"
          : "S_IS_DATA";
      success = !allCategoryList.isEmpty();

    } catch (Exception e) {
      message = "E_ADMIN"; // 코드 잘못됐을때 보여줄 내용
      System.err.println("An error occurred: " + e.getMessage());
    }

    return new ApiResponse<>(success, message, categoryListForWirte, addDataMap);

  }

  /* 좋아요 */

  // 회원레시피 좋아요
  @Override
  @Transactional(rollbackFor = Exception.class)
  public ApiResponse<Integer> insertMemberRecipeLike(RecipeLike recipeLike) throws Exception {
    String message;
    boolean success = false;
    int recipeLikeCount = 0;
    HashMap<String, Object> addDataMap = null;

    try {

      int result = 0;
      if (recipeLike.getMemberId() > 0)
        result = recipeMapper.insertMemberRecipeLike(recipeLike);

      if (result > 0) {
        recipeLikeCount = recipeMapper.getMemberRecipeLikeCount(recipeLike.getRecipeId());
      }

      message = result == 0 ? "E_ADD_DATA"
          : "S_ADD_DATA";
      success = result > 0;

    } catch (Exception e) {
      message = "E_ADMIN"; // 코드 잘못됐을때 보여줄 내용
      System.err.println("An error occurred: " + e.getMessage());
      throw new Exception(message); // Spring에 던져준다
    }

    return new ApiResponse<>(success, message, recipeLikeCount, addDataMap);

  }

  // 회원레시피 좋아요 삭제
  @Override
  @Transactional(rollbackFor = Exception.class)
  public ApiResponse<Integer> deleteMemberRecipeLike(String recipeId, int memberId) throws Exception {
    String message;
    boolean success = false;
    int recipeLikeCount = 0;
    HashMap<String, Object> addDataMap = null;

    try {

      int result = 0;
      if (memberId > 0)
        result = recipeMapper.deleteMemberRecipeLike(recipeId, memberId);

      if (result > 0) {
        recipeLikeCount = recipeMapper.getMemberRecipeLikeCount(recipeId);
      }

      message = result == 0 ? "E_DEL_DATA"
          : "S_DEL_DATA";
      success = result > 0;

    } catch (Exception e) {
      message = "E_ADMIN"; // 코드 잘못됐을때 보여줄 내용
      System.err.println("An error occurred: " + e.getMessage());
      throw new Exception(message); // Spring에 던져준다
    }

    return new ApiResponse<>(success, message, recipeLikeCount, addDataMap);

  }

  /* 관리자 */

  // 레시피 카테고리 목록(카운터수 넣어야함)
  @Override
  public ApiResponse<List<RecipeCategory>> getRecipeCategoryList(String search, int start, int display,
      String searchType) {

    String message;
    boolean success = false;

    List<RecipeCategory> recipeCategoryList = null;
    Map<String, Object> addDataMap = new HashMap<>();
    int total_cnt;

    try {
      recipeCategoryList = recipeMapper.getRecipeCategoryList(start, display, search, searchType);
      total_cnt = recipeMapper.getRecipeCategoryCount(search, searchType);
      message = (recipeCategoryList == null || recipeCategoryList.isEmpty()) ? "E_IS_DATA"
          : "S_IS_DATA";
      success = recipeCategoryList != null && !recipeCategoryList.isEmpty();
      addDataMap.put("totalCnt", total_cnt);
    } catch (Exception e) {
      message = "E_ADMIN"; // 코드 잘못됐을때 보여줄 내용
      System.err.println("An error occurred: " + e.getMessage());
    }

    return new ApiResponse<>(success, message, recipeCategoryList, addDataMap);
  }

  // 레시피 카테고리 보기
  @Override
  // 레시피 카테고리 조회
  public ApiResponse<RecipeCategory> getRecipeCategory(String recipeCategoryId) {

    String message;
    boolean success = false;

    RecipeCategory recipeCategory = null;
    Map<String, Object> addDataMap = new HashMap<>();

    try {
      recipeCategory = recipeMapper.getRecipeCategory(recipeCategoryId);

      message = recipeCategory == null ? "E_IS_DATA"
          : "S_IS_DATA";
      success = recipeCategory != null;

    } catch (Exception e) {
      message = "E_ADMIN"; // 코드 잘못됐을때 보여줄 내용
      System.err.println("An error occurred: " + e.getMessage());
    }

    return new ApiResponse<>(success, message, recipeCategory, addDataMap);

  }

  // 레시피 카테고리 추가
  @Override
  @Transactional(rollbackFor = Exception.class)
  public ApiResponse<Integer> insertRecipeCategory(RecipeCategory recipeCategory) throws Exception {
    String message;
    boolean success = false;

    int result = 0;
    Map<String, Object> addDataMap = new HashMap<>();

    try {
      result = recipeMapper.insertRecipeCategory(recipeCategory);

      message = result == 0 ? "E_ADD_DATA"
          : "E_ADD_DATA";
      success = result > 0;

    } catch (Exception e) {
      message = "E_ADMIN"; // 코드 잘못됐을때 보여줄 내용
      System.err.println("An error occurred: " + e.getMessage());
      throw new Exception(message); // Spring에 던져준다
    }

    return new ApiResponse<>(success, message, result, addDataMap);

  }

  // 레시피 카테고리 수정
  @Override
  @Transactional(rollbackFor = Exception.class)
  public ApiResponse<Integer> updateRecipeCategory(String recipeCategoryId, RecipeCategory recipeCategory)
      throws Exception {
    String message;
    boolean success = false;

    int result = 0;
    Map<String, Object> addDataMap = new HashMap<>();

    try {
      result = recipeMapper.updateRecipeCategory(recipeCategoryId, recipeCategory);

      message = result == 0 ? "E_UPDATE_DATA"
          : "S_UPDATE_DATA";
      success = result > 0;

    } catch (Exception e) {
      message = "E_ADMIN"; // 코드 잘못됐을때 보여줄 내용
      System.err.println("An error occurred: " + e.getMessage());
      throw new Exception(message); // Spring에 던져준다
    }

    return new ApiResponse<>(success, message, result, addDataMap);
  }

  // 레시피 카테고리 삭제
  @Override
  @Transactional(rollbackFor = Exception.class)
  public ApiResponse<Integer> deleteRecipeCategory(String recipeCategoryId) throws Exception {
    String message;
    boolean success = false;
    int result = 0;
    Map<String, Object> addDataMap = new HashMap<>();
    try {
      // 해당 카테고리를 사용하는 레시피가 있는지 확인
      int count = recipeMapper.countByRecipeCategoryId(recipeCategoryId);
      if (count > 0) {
        message = "ERR_CG_01";
        return new ApiResponse<>(success, message, result, addDataMap);
      }

      result = recipeMapper.deleteRecipeCategory(recipeCategoryId);

      message = (result > 0) ? "S_DEL_DATA" : "ERR_CG_02";
      success = result > 0;
    } catch (Exception e) {
      message = "E_ADMIN"; // 코드 잘못됐을때 보여줄 내용
      System.err.println("An error occurred: " + e.getMessage());
      throw new Exception(message); // Spring에 던져준다
    }
    return new ApiResponse<>(success, message, result, addDataMap);
  }

}
