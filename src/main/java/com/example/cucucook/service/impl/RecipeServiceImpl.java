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
import java.util.ArrayList;
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
import com.example.cucucook.common.FileUpload;
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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private FileUpload fileUpload;

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
                throw new Exception("재료없음");
            }
            result.put("memberRecipeIngredient", memberRecipeIngredient);

            // 과정 있으면 넣어주기
            List<MemberRecipeProcess> memberRecipeProcessList = recipeMapper.getMemberRecipeProcessList(recipeId);
            if (memberRecipeProcessList == null || memberRecipeProcessList.isEmpty()) {
                throw new Exception("과정없음");
            }

            // 수정용
            HashMap<String, Object> memberRecipeProcessForUpdate;
            List<HashMap<String, Object>> memberRecipeProcessListForUpdate = new ArrayList<HashMap<String, Object>>();

            for (MemberRecipeProcess memberRecipeProcess : memberRecipeProcessList) {
                memberRecipeProcessForUpdate = new HashMap<>();
                memberRecipeProcessForUpdate.put("image", null);
                memberRecipeProcessForUpdate.put("serverImage", memberRecipeProcess.getMemberRecipeImages());
                memberRecipeProcessForUpdate.put("isServerImgVisible", true);
                memberRecipeProcessForUpdate.put("processContents", memberRecipeProcess.getContents());
                memberRecipeProcessListForUpdate.add(memberRecipeProcessForUpdate);
            }

            result.put("memberRecipeProcessList", memberRecipeProcessList);
            result.put("memberRecipeProcessListForUpdate", memberRecipeProcessListForUpdate);

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
    public ApiResponse<Integer> insertMemberRecipe(Map<String, Object> memberRecipeInfo,
            MultipartFile thumbnail, List<MultipartFile> recipeProcessImages) throws Exception {
        String message = "";
        boolean success = false;

        int insertResult = 0;
        String resultRecipeId;
        String resultImgId;

        Map<String, Object> addDataMap = new HashMap<>();

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> recipeInfoMap = (Map<String, Object>) memberRecipeInfo.get("recipeInfo");
        List<Map<String, Object>> recipeIngredientsList = (List<Map<String, Object>>) memberRecipeInfo
                .get("recipeIngredients");
        List<String> recipeProcessContentList = (List<String>) memberRecipeInfo
                .get("recipeProcessContents");

        MemberRecipe memberRecipe = objectMapper.convertValue(recipeInfoMap, MemberRecipe.class);

        List<MemberRecipeIngredient> memberRecipeIngredientList = objectMapper.convertValue(
                recipeIngredientsList,
                new TypeReference<List<MemberRecipeIngredient>>() {
                });

        try {
            // 1.레시피 기본정보 넣기
            recipeMapper.insertMemberRecipe(memberRecipe);
            resultRecipeId = memberRecipe.getRecipeId();

            // 2. 성공시 썸네일 파일 넣기 (파일 넣기 및 DB에 파일정보 넣어주기)
            if (thumbnail != null && !"".equals(thumbnail.getOriginalFilename())) {
                String serverFileName = fileUpload.uploadFile(thumbnail, "recipe/" + resultRecipeId);

                resultImgId = insertMemberRecipeImages(thumbnail, serverFileName, resultRecipeId);

                recipeMapper.updateMemberRecipeImgId(resultRecipeId, resultImgId);
            } else {
                throw new Exception("썸네일없음");
            }

            // 3. 재료 넣기
            for (int i = 0; i < memberRecipeIngredientList.size(); i++) {
                MemberRecipeIngredient ingredient = memberRecipeIngredientList.get(i);
                ingredient.setOrderId(i); // orderId를 인덱스로 설정

                recipeMapper.insertMemberRecipeIngredient(resultRecipeId, ingredient);
            }

            // 4. 레시피 과정 넣기 (이미지 수, 과정수 일치 하는지 체크)
            int validFileCount = 0;

            for (MultipartFile file : recipeProcessImages) {
                // 유효성 검사: null이 아니고, 빈 파일이 아닌지 확인
                if (file != null && !file.isEmpty()) {
                    validFileCount++;
                }
            }

            if (validFileCount == 0)
                throw new Exception("받아온 레시피중 빈 파일이 있음");

            if (validFileCount != recipeProcessContentList.size())
                throw new Exception("레시피 과정내용 , 이미지 매치 안됨");

            for (int i = 0; i < recipeProcessContentList.size(); i++) {

                String serverFileName = fileUpload.uploadFile(recipeProcessImages.get(i), "recipe/" + resultRecipeId);

                resultImgId = insertMemberRecipeImages(recipeProcessImages.get(i), serverFileName, resultRecipeId);

                MemberRecipeProcess process = new MemberRecipeProcess();
                process.setRecipeNumber(i + 1);
                process.setContents(recipeProcessContentList.get(i));
                process.setImgId(resultImgId);

                recipeMapper.insertMemberRecipeProcess(resultRecipeId, process);
            }

            // 최종적으로 모두 등록 완료한 경우 성공
            message = "S_ADD_DATA";
            success = true;
            insertResult = 1;

        } catch (Exception e) {
            message = "E_ADD_DATA";
            System.err.println("An error occurred: " + e.getMessage());
            throw new Exception(); // Spring에 던져준다
        }

        return new ApiResponse<>(success, message, insertResult, addDataMap);

    }

    // 회원 레시피 수정 레시피, 재료, 과정, 이미지 다 포함되어야함
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Integer> updateMemberRecipe(Map<String, Object> memberRecipeInfo,
            MultipartFile thumbnail, List<MultipartFile> recipeProcessImages, String thumbnailServerImgId)
            throws Exception {
        String message = "";
        boolean success = false;

        int insertResult = 0;
        String recipeId;
        String resultImgId;

        Map<String, Object> addDataMap = new HashMap<>();

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> recipeInfoMap = (Map<String, Object>) memberRecipeInfo.get("recipeInfo");
        List<Map<String, Object>> recipeIngredientsList = (List<Map<String, Object>>) memberRecipeInfo
                .get("recipeIngredients");
        List<String> recipeProcessContentList = (List<String>) memberRecipeInfo
                .get("recipeProcessContents");

        MemberRecipe memberRecipe = objectMapper.convertValue(recipeInfoMap, MemberRecipe.class);

        List<MemberRecipeIngredient> memberRecipeIngredientList = objectMapper.convertValue(
                recipeIngredientsList,
                new TypeReference<List<MemberRecipeIngredient>>() {
                });

        try {
            // 1.레시피 기본정보 수정하기
            recipeId = memberRecipe.getRecipeId();
            recipeMapper.updateMemberRecipe(memberRecipe);

            // 2. 받아온 파일이 있다면 엎어쳐주기 아니면 해당 부분 넘어가기
            // 받아온 파일이 있을때 폼데이터로 넘어온 기존 서버 이미지값가지고 처리
            // 1) 해당 이미지값에 해당하는 이미지 서버 데이터 삭제
            // 2) 해당 이미지값에 해당하는 이미지테이블 db삭제
            // 3) 받아온 파일 처리 업로드랑 동일하게 처리
            if (thumbnail != null && !"".equals(thumbnail.getOriginalFilename())) {
                if (!"".equals(thumbnailServerImgId)) {
                    MemberRecipeImages memberRecipeImages = recipeMapper.getMemberRecipeImages(thumbnailServerImgId);
                    fileUpload.deleteFile(memberRecipeImages);
                    recipeMapper.deleteMemberRecipeImages(thumbnailServerImgId);
                }
                String serverFileName = fileUpload.uploadFile(thumbnail, "recipe/" +
                        recipeId);

                resultImgId = insertMemberRecipeImages(thumbnail, serverFileName, recipeId);

                recipeMapper.updateMemberRecipeImgId(recipeId, resultImgId);
            } else {
                throw new Exception("썸네일없음");
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
            // 해당 레시피에 있는 과정 일괄 삭제 후 다시 insert

            // int validFileCount = 0;

            // for (MultipartFile file : recipeProcessImages) {
            // // 유효성 검사: null이 아니고, 빈 파일이 아닌지 확인
            // if (file != null && !file.isEmpty()) {
            // validFileCount++;
            // }
            // }

            // if (validFileCount == 0)
            // throw new Exception("받아온 레시피중 빈 파일이 있음");

            // if (validFileCount != recipeProcessContentList.size())
            // throw new Exception("레시피 과정내용 , 이미지 매치 안됨");

            recipeMapper.deleteMemberRecipeProcess(recipeId, "");
            for (int i = 0; i < recipeProcessContentList.size(); i++) {

                // String serverFileName = fileUpload.uploadFile(recipeProcessImages.get(i),
                // "recipe/" + recipeId);

                // resultImgId = insertMemberRecipeImages(recipeProcessImages.get(i),
                // serverFileName, recipeId);

                MemberRecipeProcess process = new MemberRecipeProcess();
                process.setRecipeNumber(i + 1);
                process.setContents(recipeProcessContentList.get(i));
                // process.setImgId(resultImgId);

                recipeMapper.insertMemberRecipeProcess(recipeId, process);
            }

            // 최종적으로 모두 등록 완료한 경우 성공
            message = "S_UPDATE_DATA";
            success = true;
            insertResult = 1;

        } catch (Exception e) {
            message = "E_UPDATE_DATA";
            System.err.println("An error occurred: " + e.getMessage());
            throw new Exception(); // Spring에 던져준다
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
                fileUpload.deleteFile(memberRecipeImages);
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
        } catch (Exception e) {
            message = "E_DEL_DATA"; // 코드 잘못됐을때 보여줄 내용
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace(); // 오류 자세히 확인할때만 주석해제하고사용
            throw new Exception(); // Spring에 던져준다
        }

        return new ApiResponse<>(success, message, deleteResult, addDataMap);

    }

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
            System.err.println("An error occurred: " + e.getMessage());
            throw new Exception(); // Spring에 던져준다
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
            throw new Exception(); // Spring에 던져준다
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
            throw new Exception(); // Spring에 던져준다
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
            throw new Exception(); // Spring에 던져준다
        }

        return new ApiResponse<>(success, message, deleteComment, addDataMap);
    }

    // 레시피 카테고리 목록(카운터수 넣어야함)
    @Override
    public ApiResponse<List<RecipeCategory>> getRecipeCategoryList(String search, int start, int display) {

        String message;
        boolean success = false;

        List<RecipeCategory> recipeCategoryList = null;
        Map<String, Object> addDataMap = new HashMap<>();

        try {
            recipeCategoryList = recipeMapper.getRecipeCategoryList(start, display);

            message = (recipeCategoryList == null || recipeCategoryList.isEmpty()) ? "E_IS_DATA"
                    : "S_IS_DATA";
            success = recipeCategoryList != null && !recipeCategoryList.isEmpty();
        } catch (Exception e) {
            message = "E_ADMIN"; // 코드 잘못됐을때 보여줄 내용
            System.err.println("An error occurred: " + e.getMessage());
        }

        return new ApiResponse<>(success, message, recipeCategoryList, addDataMap);
    }

    // 레시피 카테고리 수정
    @Override
    @Transactional(rollbackFor = Exception.class)
    public HashMap<String, Object> insertRecipeCategory(RecipeCategory recipeCategory) throws Exception {
        HashMap<String, Object> result = new HashMap<>();
        return result;
    }

    // 레시피 카테고리 삭제
    @Override
    @Transactional(rollbackFor = Exception.class)
    public HashMap<String, Object> updateRecipeCategory(RecipeCategory recipeCategory) throws Exception {
        HashMap<String, Object> result = new HashMap<>();
        return result;
    }

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
            List<RecipeCategory> allCategoryList = recipeMapper.getRecipeCategoryList(0, 0);
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
        }

        return new ApiResponse<>(success, message, recipeLikeCount, addDataMap);

    }

    // 이미지 정보 DB저장
    public String insertMemberRecipeImages(MultipartFile imageFile, String serverFileName, String recipeId)
            throws Exception {
        MemberRecipeImages memberRecipeImage = new MemberRecipeImages();
        memberRecipeImage.setOrgImgName(fileUpload.extractOriginalFileName(imageFile.getOriginalFilename()));
        memberRecipeImage.setServerImgName(serverFileName);
        memberRecipeImage.setExtension(fileUpload.extractExtension(imageFile.getOriginalFilename(),
                imageFile.getContentType()));
        memberRecipeImage.setImgFileSize(Long.toString(imageFile.getSize()));
        memberRecipeImage.setServerImgPath(fileUpload.getFileDir("recipe/" + recipeId));
        memberRecipeImage.setWebImgPath(fileUpload.getFileWebDir("recipe/" + recipeId));
        recipeMapper.insertMemberRecipeImages(memberRecipeImage);

        return memberRecipeImage.getImgId();

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
}
