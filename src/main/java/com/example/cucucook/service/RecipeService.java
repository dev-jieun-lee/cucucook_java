package com.example.cucucook.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.springframework.web.multipart.MultipartFile;

import com.example.cucucook.common.ApiResponse;
import com.example.cucucook.domain.MemberRecipe;
import com.example.cucucook.domain.PublicRecipe;
import com.example.cucucook.domain.RecipeCategory;
import com.example.cucucook.domain.RecipeComment;
import com.example.cucucook.domain.RecipeLike;

public interface RecipeService {
  // 기존 레시피 목록
  public ApiResponse<List<PublicRecipe>> getPublicRecipeList(String search, int start,
      int display, String recipeCategoryId);

  // 회원 레시피 목록
  public ApiResponse<List<MemberRecipe>> getMemberRecipeList(String search, String recipeCategoryId, int start,
      int display, String orderby, int memberId);

  // 기존 레시피 목록
  public ApiResponse<PublicRecipe> getPublicRecipe(String search, int start,
      int display);

  // 회원 레시피 상세보기
  // 1. 내부,외부에 따라 처리방식 다르게됨
  // 2. 레시피, 재료, 과정, 이미지, 레시피 찜수 다 포함되어야함
  public ApiResponse<HashMap<String, Object>> getMemberRecipe(String recipeId, int memberId, boolean isUpdate);

  // 회원 레시피 글쓰기 레시피, 재료, 과정, 이미지, 레시피 찜수 다 포함되어야함
  public ApiResponse<Integer> insertMemberRecipe(Map<String, Object> memberRecipeInfo,
      MultipartFile thumbnail, List<MultipartFile> recipeProcessImages) throws Exception;

  // 회원 레시피 수정 레시피, 재료, 과정, 이미지 다 포함되어야함
  public ApiResponse<Integer> updateMemberRecipe(Map<String, Object> memberRecipeInfo,
      MultipartFile thumbnail, List<MultipartFile> recipeProcessImages, String thumbnailServerImgId)
      throws Exception;

  // 회원 레시피 삭제 레시피, 재료, 과정, 이미지, 댓글, 레시피 찜수 다 포함되어야함
  public ApiResponse<Integer> deleteMemberRecipe(String recipeId) throws Exception;

  // 회원레시피 별 댓글목록
  public ApiResponse<List<RecipeComment>> getRecipeCommentList(String recipeId, int start, int display);

  // 회원레시피 댓글
  public ApiResponse<RecipeComment> getRecipeComment(String recipeId, String commentId);

  // 회원레시피 댓글 쓰기
  public ApiResponse<Integer> insertRecipeComment(RecipeComment recipeComment) throws Exception;

  // 회원레시피 댓글 수정
  public ApiResponse<Integer> updateRecipeComment(RecipeComment recipeComment) throws Exception;

  // 회원레시피 댓글 삭제
  public ApiResponse<Integer> deleteRecipeComment(String recipeId, String commentId) throws Exception;

  // 회원레시피 댓글 삭제(본댓글에 대댓글이 남겨져 있는 경우)
  public ApiResponse<Integer> deleteRecipeCommentHasChild(String recipeId, String commentId) throws Exception;

  // 레시피 카테고리 목록(카운터수 넣어야함)
  public ApiResponse<List<RecipeCategory>> getRecipeCategoryList(String search, int start, int display);

  // 레시피 카테고리 수정
  public HashMap<String, Object> insertRecipeCategory(RecipeCategory recipeCategory) throws Exception;

  // 레시피 카테고리 삭제
  public HashMap<String, Object> updateRecipeCategory(RecipeCategory recipeCategory) throws Exception;

  // 레시피 카테고리 목록(카운트별 레시피 카운트수 포함)
  public ApiResponse<List<RecipeCategory>> getRecipeCategoryListWithMemberRecipeCount(
      @Param("search") String search);

  // 레시피 카테고리 목록(레시피 작성을 위함)
  public ApiResponse<HashMap<String, Object>> getRecipeCategoryListForWrite();

  // 회원레시피 좋아요
  public ApiResponse<Integer> insertMemberRecipeLike(RecipeLike recipeLike) throws Exception;

  // 회원레시피 좋아요 삭제
  public ApiResponse<Integer> deleteMemberRecipeLike(String recipeId, int memberId) throws Exception;

}
