package com.example.cucucook.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.example.cucucook.domain.Board;
import com.example.cucucook.domain.RecipeComment;
import com.example.cucucook.domain.RecipeLike;

@Mapper
public interface MypageMapper {

    // 회원이 쓴 글 갯수
    int getMemberBoardCount(@Param("memberId") int memberId);

    // 회원이 쓴 글 목록
    List<Board> getMemberBoardList(@Param("memberId") int memberId);

    // 회원이 쓴 댓글 갯수
    int getRecipeCommentCount(@Param("memberId") int memberId);

    // 회원이 쓴 댓글 목록
    List<RecipeComment> getRecipeCommentList(@Param("memberId") int memberId, @Param("start") int start, @Param("display") int display);

    // 회원이 찜한 레시피 갯수
    int getMemberRecipeLikeCount(@Param("memberId") int memberId);

    // 회원 레시피 찜 목록
    List<RecipeLike> getRecipeLikeList(@Param("memberId") int memberId, @Param("start") int start, @Param("display") int display);

    // 회원 레시피 찜 보기
    RecipeLike getRecipeLike(@Param("memberId") int memberId, @Param("recipeId") String recipeId);

    // 회원 레시피 찜 추가
    void insertRecipeLike(@Param("recipeLike") RecipeLike recipeLike);

    // 회원 레시피 찜 삭제
    void deleteRecipeLike(@Param("memberId") int memberId, @Param("recipeId") String recipeId);

}
