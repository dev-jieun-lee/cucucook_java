package com.example.cucucook.service;

import com.example.cucucook.domain.Board;
import com.example.cucucook.domain.RecipeComment;
import com.example.cucucook.domain.RecipeLike;

import java.util.List;

public interface MypageService {

    // 회원 게시물 관련 메서드
    int getMemberBoardCount(Long memberId);
    List<Board> getMemberBoardList(Long memberId);

    // 회원 댓글 관련 메서드
    int getRecipeCommentCount(Long memberId);
    List<RecipeComment> getRecipeCommentList(Long memberId, int start, int display);

    // 회원 레시피 찜 관련 메서드
    int getMemberRecipeLikeCount(Long memberId);
    List<RecipeLike> getRecipeLikeList(Long memberId, int start, int display);
    RecipeLike getRecipeLike(Long memberId, String recipeId);
    void addRecipeLike(RecipeLike recipeLike);
    void removeRecipeLike(Long memberId, String recipeId);
}
