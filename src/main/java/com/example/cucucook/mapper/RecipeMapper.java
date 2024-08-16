package com.example.cucucook.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.cucucook.domain.MemberRecipe;
import com.example.cucucook.domain.MemberRecipeImages;
import com.example.cucucook.domain.MemberRecipeIngredient;
import com.example.cucucook.domain.MemberRecipeProcess;
import com.example.cucucook.domain.RecipeCategory;
import com.example.cucucook.domain.RecipeComment;

@Mapper
public interface RecipeMapper {

    // 외부레시피 내부레시피 반환
    int getMemberRecipeDivision(@Param("recipeId") String recipeId);

    // 레시피 수
    int getMemberRecipeCount(@Param("recipeCategoryId") String recipeCategoryId);

    // 회원레시피 목록
    List<MemberRecipe> getMemberRecipeList(@Param("search") String search,
            @Param("recipeCategoryId") String recipeCategoryId,
            @Param("start") int start,
            @Param("display") int display,
            @Param("orderby") String orderby);

    // 레시피 상세 보기
    MemberRecipe getMemberRecipe(@Param("recipeId") String recipeId);

    // 회원레시피 글쓰기
    int insertMemberRecipe(@Param("memberRecipe") MemberRecipe memberRecipe);

    // 회원레시피 수정
    int updateMemberRecipe(@Param("memberRecipe") MemberRecipe memberRecipe);

    // 회원레시피 삭제
    int deleteMemberRecipe(@Param("recipeId") String recipeId);

    // 회원레시피 재료 목록
    List<MemberRecipeIngredient> getMemberRecipeIngredientList(@Param("recipeId") String recipeId);

    // 회원레시피 재료 보기
    MemberRecipeIngredient getMemberRecipeIngredient(@Param("recipeId") String recipeId, @Param("ingredientId") String ingredientId);

    // 회원레시피 재료 넣기
    int insertMemberRecipeIngredient(@Param("memberRecipeIngredient") MemberRecipeIngredient memberRecipeIngredient);

    // 회원레시피 재료 수정
    int updateMemberRecipeIngredient(@Param("memberRecipeIngredient") MemberRecipeIngredient memberRecipeIngredient);

    // 회원레시피 재료 삭제
    int deleteMemberRecipeIngredient(@Param("recipeId") String recipeId);

    // 회원레시피 과정 목록
    List<MemberRecipeProcess> getMemberRecipeProcessList(@Param("recipeId") String recipeId);

    // 회원레시피 과정 보기
    MemberRecipeProcess getMemberRecipeProcess(@Param("recipeId") String recipeId, @Param("recipeProcessId") String recipeProcessId);

    // 회원레시피 과정 넣기
    int insertMemberRecipeProcess(@Param("memberRecipeProcess") MemberRecipeProcess memberRecipeProcess);

    // 회원레시피 과정 수정
    int updateMemberRecipeProcess(@Param("memberRecipeProcess") MemberRecipeProcess memberRecipeProcess);

    // 회원레시피 과정 삭제
    int deleteMemberRecipeProcess(@Param("recipeId") String recipeId);

    // 레시피 이미지 보기
    MemberRecipeImages getMemberRecipeImages(@Param("imgId") String imgId);

    // 레시피 이미지 넣기
    int insertMemberRecipeImages(@Param("memberRecipeImages") MemberRecipeImages memberRecipeImages);

    // 레시피 이미지 삭제
    int deleteMemberRecipeImages(@Param("recipeId") String recipeId);

    // 레시피 댓글 수
    int getRecipeCommentCount(@Param("recipeId") String recipeId);

    // 회원레시피 댓글 목록
    List<RecipeComment> getRecipeCommentList(@Param("recipeId") String recipeId, @Param("start") int start, @Param("display") int display, @Param("pCommentid") String pCommentid);

    // 회원레시피 댓글 보기
    RecipeComment getRecipeComment(@Param("recipeId") String recipeId);

    // 회원레시피 댓글 쓰기
    int insertRecipeComment(@Param("recipeComment") RecipeComment recipeComment);

    // 회원레시피 댓글 수정
    int updateRecipeComment(@Param("recipeComment") RecipeComment recipeComment);

    // 회원레시피 댓글 삭제
    int deleteRecipeComment(@Param("recipeId") String recipeId);

    // 레시피 카테고리 수
    int getRecipeCategoryCount();

    // 레시피 카테고리 목록
    List<RecipeCategory> getRecipeCategoryList(@Param("start") int start, @Param("display") int display);

    // 레시피 카테고리 보기
    RecipeCategory getRecipeCategory(@Param("recipeCategoryId") String recipeCategoryId);

    // 레시피 카테고리 추가
    int insertRecipeCategory(@Param("recipeCategory") RecipeCategory recipeCategory);

    // 레시피 카테고리 수정
    int updateRecipeCategory(@Param("recipeCategory") RecipeCategory recipeCategory);

    // 레시피 카테고리 삭제
    int deleteRecipeCategory(@Param("recipeCategoryId") String recipeCategoryId);

    // 레시피 찜 수
    int getMemberRecipeLikeCount(@Param("recipeId") String recipeId);
}
