package com.example.cucucook.service;

import java.util.HashMap;
import java.util.List;

import com.example.cucucook.common.ApiResponse;
import com.example.cucucook.domain.MemberRecipe;
import com.example.cucucook.domain.PublicRecipe;
import com.example.cucucook.domain.RecipeCategory;
import com.example.cucucook.domain.RecipeComment;

public interface RecipeService {

        // 회원 레시피 목록
        public ApiResponse<List<MemberRecipe>> getMemberRecipeList(String search, String recipeCategoryId, int start,
                        int display, String orderby);

        // 회원 레시피 목록 외부
        public ApiResponse<List<PublicRecipe>> getPublicRecipeList(String search, int start,
                        int display);

        // 회원 레시피 상세보기
        // 1. 내부,외부에 따라 처리방식 다르게됨
        // 2. 레시피, 재료, 과정, 이미지, 레시피 찜수 다 포함되어야함
        public ApiResponse<HashMap<String, Object>> getMemberRecipe(String recipeId);

        // 회원 레시피상세보기 외부 API
        public ApiResponse<PublicRecipe> getPublicRecipe(String search, int start,
                        int display);

        // 회원 레시피 글쓰기 레시피, 재료, 과정, 이미지, 레시피 찜수 다 포함되어야함
        public ApiResponse<Integer> insertMemberRecipe(MemberRecipe memberRecipe);

        // 회원 레시피 수정 레시피, 재료, 과정, 이미지 다 포함되어야함
        public ApiResponse<Integer> updateMemberRecipe(MemberRecipe memberRecipe);

        // 회원 레시피 삭제 레시피, 재료, 과정, 이미지, 댓글, 레시피 찜수 다 포함되어야함
        public ApiResponse<Integer> deleteMemberRecipe(String recipeId);

        // 회원레시피 댓글목록(대댓글, 카운터수 다 넣어야함)
        public HashMap<String, Object> getRecipeCommentList(String recipeId, int start, int display, String pCommentid);

        // 회원레시피 댓글 쓰기
        public HashMap<String, Object> insertRecipeComment(RecipeComment recipeComment);

        // 회원레시피 댓글 수정
        public HashMap<String, Object> updateRecipeComment(RecipeComment recipeComment);

        // 회원레시피 댓글 삭제
        public HashMap<String, Object> deleteRecipeComment(String recipeId);

        // 레시피 카테고리 목록(카운터수 넣어야함)
        public HashMap<String, Object> getRecipeCategoryList(int start, int display);

        // 레시피 카테고리 수정
        public HashMap<String, Object> insertRecipeCategory(RecipeCategory recipeCategory);

        // 레시피 카테고리 삭제
        public HashMap<String, Object> updateRecipeCategory(RecipeCategory recipeCategory);
}
