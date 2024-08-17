package com.example.cucucook.service.impl;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.cucucook.common.ApiResponse;
import com.example.cucucook.domain.MemberRecipe;
import com.example.cucucook.domain.MemberRecipeImages;
import com.example.cucucook.domain.MemberRecipeIngredient;
import com.example.cucucook.domain.MemberRecipeProcess;
import com.example.cucucook.domain.RecipeCategory;
import com.example.cucucook.domain.RecipeComment;
import com.example.cucucook.mapper.RecipeMapper;
import com.example.cucucook.service.RecipeService;

@Service
public class RecipeServiceImpl implements RecipeService {

    @Autowired
    private RecipeMapper recipeMapper;

    //회원 레시피 목록 (내부 외부 분기)
    @Override
    public ApiResponse<List<MemberRecipe>> getMemberRecipeList(String search, String recipeCategoryId, int start, int display, String orderby, String division) {

        start = start > 0 ? start : 1;
        display = display > 0 ? display : 10;

        List<MemberRecipe> memberRecipeList = !"OPEN".equals(division) ? getMemberRecipePrivateApiList(search, recipeCategoryId, start, display, orderby) : getMemberRecipeOpenApiList(search, start, display);
        String message = (memberRecipeList == null || memberRecipeList.isEmpty()) ? "회원 레시피 목록이 없습니다." : "회원 레시피 목록 조회 성공";
        boolean success = memberRecipeList != null && !memberRecipeList.isEmpty();

        return new ApiResponse<>(success, message, memberRecipeList);
    }

    //회원 레시피 목록 내부API
    @Override
    public List<MemberRecipe> getMemberRecipePrivateApiList(String search, String recipeCategoryId, int start, int display, String orderby) {
        return recipeMapper.getMemberRecipeList(search, recipeCategoryId, start, display, orderby);
    }

    //회원 레시피 목록 외부API
    @Override
    public List<MemberRecipe> getMemberRecipeOpenApiList(String search, int start, int display) {
        return null;
    }

    //회원 레시피 상세보기
    // 1. 내부,외부에 따라 처리방식 다르게됨
    // 2. 레시피, 재료, 과정, 이미지, 레시피 찜수 다 포함되어야함
    @Override
    public ApiResponse<HashMap<String, Object>> getMemberRecipe(String recipeId) {
        //recipeExist == 0 내부 recipeExist > 0 외부
        int recipeExist = recipeMapper.getMemberRecipeDivision(recipeId);

        HashMap<String, Object> memberRecipe = recipeExist == 1 ? getMemberRecipePrivateApi(recipeId) : getMemberRecipeOpenApi(recipeId);

        String message = (memberRecipe == null || memberRecipe.isEmpty()) ? "등록된 회원 레시피가 없습니다" : "회원 레시피 조회 성공";
        boolean success = memberRecipe != null && !memberRecipe.isEmpty();

        return new ApiResponse<>(success, message, memberRecipe);
    }

    //회원 레시피 상세보기 내부API
    @Override
    public HashMap<String, Object> getMemberRecipePrivateApi(String recipeId) {

        HashMap<String, Object> result = new HashMap<>();

        MemberRecipe memberRecipe = recipeMapper.getMemberRecipe(recipeId);

        if (memberRecipe == null) {
            return result;
        }

        result.put("memberRecipe", memberRecipe);

        //이미지 있으면 넣어주기
        MemberRecipeImages memberRecipeImages = recipeMapper.getMemberRecipeImages(memberRecipe.getImgId());
        if (memberRecipeImages != null) {
            result.put("memberRecipeImages", memberRecipeImages);
        }

        //재료 있으면 넣어주기
        List<MemberRecipeIngredient> memberRecipeIngredient = recipeMapper.getMemberRecipeIngredientList(recipeId);
        if (memberRecipeIngredient != null && !memberRecipeIngredient.isEmpty()) {
            result.put("memberRecipeIngredient", memberRecipeIngredient);
        }

        //과정 있으면 넣어주기
        List<MemberRecipeProcess> memberRecipeProcessList = recipeMapper.getMemberRecipeProcessList(recipeId);
        if (memberRecipeProcessList != null && !memberRecipeProcessList.isEmpty()) {
            HashMap<String, Object> memberRecipeProcessHashMap = new HashMap<>();
            // 각 과정마다 썸네일이 있어서 리스트 for문 돌려 새로운 hashmap 만들어서 넣기
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

        //레시피 찜수 넣어주기
        result.put("recipeLike", recipeMapper.getMemberRecipeLikeCount(recipeId));

        return result;
    }

    //회원 레시피 상세보기 외부API
    @Override
    public HashMap<String, Object> getMemberRecipeOpenApi(String recipeId) {
        HashMap<String, Object> result = new HashMap<>();
        return result;
    }

    //회원 레시피 글쓰기 레시피, 재료, 과정, 이미지, 레시피 찜수 다 포함되어야함
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Integer> insertMemberRecipe(MemberRecipe memberRecipe) {

        int insertMemberRecipe = recipeMapper.insertMemberRecipe(memberRecipe);
        String message = (insertMemberRecipe == 0) ? "회원 레시피 등록 실패" : "회원 레시피 등록 성공";
        boolean success = insertMemberRecipe == 1;

        return new ApiResponse<>(success, message, 1);

    }

    //회원 레시피 수정 레시피, 재료, 과정, 이미지 다 포함되어야함
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Integer> updateMemberRecipe(MemberRecipe memberRecipe) {
        int updateMemberRecipe = recipeMapper.updateMemberRecipe(memberRecipe);
        String message = (updateMemberRecipe == 0) ? "회원 레시피 수정 실패" : "회원 레시피 수정 성공";
        boolean success = updateMemberRecipe == 1;
        return new ApiResponse<>(success, message, updateMemberRecipe);
    }

    //회원 레시피 삭제 레시피, 재료, 과정, 이미지, 댓글, 레시피 찜수 다 포함되어야함
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Integer> deleteMemberRecipe(String recipeId) {
        int deleteMemberRecipe = recipeMapper.deleteMemberRecipe(recipeId);
        String message = (deleteMemberRecipe == 0) ? "회원 레시피 삭제 실패" : "회원 레시피 삭제 성공";
        boolean success = deleteMemberRecipe == 1;
        return new ApiResponse<>(success, message, deleteMemberRecipe);
    }

    //회원레시피 댓글목록(대댓글, 카운터수 다 넣어야함)
    @Override
    public HashMap<String, Object> getRecipeCommentList(String recipeId, int start, int display, String pCommentid) {
        HashMap<String, Object> result = new HashMap<>();
        return result;
    }

    //회원레시피 댓글 쓰기
    @Override
    public HashMap<String, Object> insertRecipeComment(RecipeComment recipeComment) {
        HashMap<String, Object> result = new HashMap<>();
        return result;
    }

    //회원레시피 댓글 수정
    @Override
    public HashMap<String, Object> updateRecipeComment(RecipeComment recipeComment) {
        HashMap<String, Object> result = new HashMap<>();
        return result;
    }

    //회원레시피 댓글 삭제
    @Override
    public HashMap<String, Object> deleteRecipeComment(String recipeId) {
        HashMap<String, Object> result = new HashMap<>();
        return result;
    }

    //레시피 카테고리 목록(카운터수 넣어야함)
    @Override
    public HashMap<String, Object> getRecipeCategoryList(int start, int display) {
        HashMap<String, Object> result = new HashMap<>();
        return result;
    }

    // 레시피 카테고리 수정
    @Override
    public HashMap<String, Object> insertRecipeCategory(RecipeCategory recipeCategory) {
        HashMap<String, Object> result = new HashMap<>();
        return result;
    }

    // 레시피 카테고리 삭제
    @Override
    public HashMap<String, Object> updateRecipeCategory(RecipeCategory recipeCategory) {
        HashMap<String, Object> result = new HashMap<>();
        return result;
    }

}
