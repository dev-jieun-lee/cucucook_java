package com.example.cucucook.service.impl;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.cucucook.common.ApiResponse;
import com.example.cucucook.domain.MemberRecipe;
import com.example.cucucook.domain.MemberRecipeImages;
import com.example.cucucook.domain.MemberRecipeIngredient;
import com.example.cucucook.domain.MemberRecipeProcess;
import com.example.cucucook.domain.PublicRecipe;
import com.example.cucucook.domain.RecipeCategory;
import com.example.cucucook.domain.RecipeComment;
import com.example.cucucook.mapper.RecipeMapper;
import com.example.cucucook.service.RecipeService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

@Service
public class RecipeServiceImpl implements RecipeService {
    // ������ ����api��������
    @Value("${recipe.open.api.url}")
    private String openApiUrl;
    @Value("${recipe.open.api.key}")
    private String openApiKey;

    @Autowired
    private RecipeMapper recipeMapper;

    // ȸ�� ������ ���
    @Override
    public ApiResponse<List<MemberRecipe>> getMemberRecipeList(String search, String recipeCategoryId, int start,
            int display, String orderby) {

        List<MemberRecipe> memberRecipeList = recipeMapper.getMemberRecipeList(search, recipeCategoryId, start, display,
                orderby);
        String message = (memberRecipeList == null || memberRecipeList.isEmpty()) ? "ȸ�� ������ ����� �����ϴ�."
                : "ȸ�� ������ ��� ��ȸ ����";
        boolean success = memberRecipeList != null && !memberRecipeList.isEmpty();

        return new ApiResponse<>(success, message, memberRecipeList);
    }

    // ȸ�� ������ ��� �ܺ�API
    @Override
    public ApiResponse<List<PublicRecipe>> getPublicRecipeList(String search, int start, int display) {
        String result_json = "";
        String rcpNm = "".equals(search) ? "" : "/RCP_NM=" + search;

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

        } catch (Exception e) {
            e.printStackTrace();
        }

        Gson gson = new Gson();
        JsonElement jsonElement = gson.fromJson(result_json, JsonElement.class);

        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonObject cookRcp01 = jsonObject.getAsJsonObject("COOKRCP01");
        JsonArray rowArray = cookRcp01.getAsJsonArray("row");
        JsonObject result = cookRcp01.getAsJsonObject("RESULT");
        Type recipeListType = new TypeToken<List<PublicRecipe>>() {
        }.getType();

        List<PublicRecipe> publicRecipeList = gson.fromJson(rowArray, recipeListType);

        String message = result.get("MSG").getAsString();
        String code = result.get("CODE").getAsString();
        boolean success = "INFO-000".equals(code);

        return new ApiResponse<>(success, message, publicRecipeList);
    }

    // ȸ�� ������ �󼼺���
    // 1. ����,�ܺο� ���� ó����� �ٸ��Ե�
    // 2. ������, ���, ����, �̹���, ������ ��� �� ���ԵǾ����
    @Override
    public ApiResponse<HashMap<String, Object>> getMemberRecipe(String recipeId) {
        // recipeExist == 0 ���� recipeExist > 0 �ܺ�
        int recipeExist = recipeMapper.getMemberRecipeDivision(recipeId);

        HashMap<String, Object> result = new HashMap<>();

        MemberRecipe memberRecipe = recipeMapper.getMemberRecipe(recipeId);

        result.put("memberRecipe", memberRecipe);

        // �̹��� ������ �־��ֱ�
        MemberRecipeImages memberRecipeImages = recipeMapper.getMemberRecipeImages(memberRecipe.getImgId());
        if (memberRecipeImages != null) {
            result.put("memberRecipeImages", memberRecipeImages);
        }

        // ��� ������ �־��ֱ�
        List<MemberRecipeIngredient> memberRecipeIngredient = recipeMapper.getMemberRecipeIngredientList(recipeId);
        if (memberRecipeIngredient != null && !memberRecipeIngredient.isEmpty()) {
            result.put("memberRecipeIngredient", memberRecipeIngredient);
        }

        // ���� ������ �־��ֱ�
        List<MemberRecipeProcess> memberRecipeProcessList = recipeMapper.getMemberRecipeProcessList(recipeId);
        if (memberRecipeProcessList != null && !memberRecipeProcessList.isEmpty()) {
            HashMap<String, Object> memberRecipeProcessHashMap = new HashMap<>();
            // �� �������� ������� �־ ����Ʈ for�� ���� ���ο� hashmap ���� �ֱ�
            for (MemberRecipeProcess memberRecipeProcess : memberRecipeProcessList) {
                memberRecipeProcessHashMap.put("memberRecipeProcess", memberRecipeProcess);
                String recipeProcessImgId = memberRecipeProcess.getImgId();
                MemberRecipeImages memberRecipeProcessImages = recipeMapper.getMemberRecipeImages(recipeProcessImgId);
                if (memberRecipeImages != null) {
                    memberRecipeProcessHashMap.put("memberRecipeProcessImages", memberRecipeProcessImages);
                }
            }
            result.put("memberRecipeProcessList", memberRecipeProcessHashMap);
        }

        // ������ ��� �־��ֱ�
        result.put("recipeLike", recipeMapper.getMemberRecipeLikeCount(recipeId));

        HashMap<String, Object> memberRecipeHashMap = result;

        String message = (memberRecipeHashMap == null || memberRecipeHashMap.isEmpty()) ? "��ϵ� ȸ�� �����ǰ� �����ϴ�"
                : "ȸ�� ������ ��ȸ ����";
        boolean success = memberRecipeHashMap != null && !memberRecipeHashMap.isEmpty();

        return new ApiResponse<>(success, message, memberRecipeHashMap);
    }

    // ȸ�� ������ ��� �ܺ�API
    @Override
    public ApiResponse<PublicRecipe> getPublicRecipe(String search, int start,
            int display) {
        String result_json = "";
        String rcpNm = "".equals(search) ? "" : "/RCP_NM=" + search;

        try {
            String urlString = openApiUrl + "/" + openApiKey + "/COOKRCP01/json/" + 1 + "/" + 1 + rcpNm;
            URI uri = new URI(urlString);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            result_json = response.body();

        } catch (Exception e) {
            e.printStackTrace();
        }

        Gson gson = new Gson();
        JsonElement jsonElement = gson.fromJson(result_json, JsonElement.class);

        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonObject cookRcp01 = jsonObject.getAsJsonObject("COOKRCP01");
        JsonArray rowArray = cookRcp01.getAsJsonArray("row");
        JsonObject result = cookRcp01.getAsJsonObject("RESULT");

        PublicRecipe publicRecipe = gson.fromJson(rowArray.get(0), PublicRecipe.class);
        String message = result.get("MSG").getAsString();
        String code = result.get("CODE").getAsString();
        boolean success = "INFO-000".equals(code);

        return new ApiResponse<>(success, message, publicRecipe);
    }

    // ȸ�� ������ �۾��� ������, ���, ����, �̹���, ������ ��� �� ���ԵǾ����
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Integer> insertMemberRecipe(MemberRecipe memberRecipe) {

        int insertMemberRecipe = recipeMapper.insertMemberRecipe(memberRecipe);
        String message = (insertMemberRecipe == 0) ? "ȸ�� ������ ��� ����" : "ȸ�� ������ ��� ����";
        boolean success = insertMemberRecipe == 1;

        return new ApiResponse<>(success, message, 1);

    }

    // ȸ�� ������ ���� ������, ���, ����, �̹��� �� ���ԵǾ����
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Integer> updateMemberRecipe(MemberRecipe memberRecipe) {
        int updateMemberRecipe = recipeMapper.updateMemberRecipe(memberRecipe);
        String message = (updateMemberRecipe == 0) ? "ȸ�� ������ ���� ����" : "ȸ�� ������ ���� ����";
        boolean success = updateMemberRecipe == 1;
        return new ApiResponse<>(success, message, updateMemberRecipe);
    }

    // ȸ�� ������ ���� ������, ���, ����, �̹���, ���, ������ ��� �� ���ԵǾ����
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Integer> deleteMemberRecipe(String recipeId) {
        int deleteMemberRecipe = recipeMapper.deleteMemberRecipe(recipeId);
        String message = (deleteMemberRecipe == 0) ? "ȸ�� ������ ���� ����" : "ȸ�� ������ ���� ����";
        boolean success = deleteMemberRecipe == 1;
        return new ApiResponse<>(success, message, deleteMemberRecipe);
    }

    // ȸ�������� ��۸��(����, ī���ͼ� �� �־����)
    @Override
    public HashMap<String, Object> getRecipeCommentList(String recipeId, int start, int display, String pCommentid) {
        HashMap<String, Object> result = new HashMap<>();
        return result;
    }

    // ȸ�������� ��� ����
    @Override
    public HashMap<String, Object> insertRecipeComment(RecipeComment recipeComment) {
        HashMap<String, Object> result = new HashMap<>();
        return result;
    }

    // ȸ�������� ��� ����
    @Override
    public HashMap<String, Object> updateRecipeComment(RecipeComment recipeComment) {
        HashMap<String, Object> result = new HashMap<>();
        return result;
    }

    // ȸ�������� ��� ����
    @Override
    public HashMap<String, Object> deleteRecipeComment(String recipeId) {
        HashMap<String, Object> result = new HashMap<>();
        return result;
    }

    // ������ ī�װ� ���(ī���ͼ� �־����)
    @Override
    public HashMap<String, Object> getRecipeCategoryList(int start, int display) {
        HashMap<String, Object> result = new HashMap<>();
        return result;
    }

    // ������ ī�װ� ����
    @Override
    public HashMap<String, Object> insertRecipeCategory(RecipeCategory recipeCategory) {
        HashMap<String, Object> result = new HashMap<>();
        return result;
    }

    // ������ ī�װ� ����
    @Override
    public HashMap<String, Object> updateRecipeCategory(RecipeCategory recipeCategory) {
        HashMap<String, Object> result = new HashMap<>();
        return result;
    }

}
