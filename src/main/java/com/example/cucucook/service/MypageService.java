package com.example.cucucook.service;

import java.util.List;

import com.example.cucucook.domain.Board;
import com.example.cucucook.domain.RecipeComment;
import com.example.cucucook.domain.RecipeLike;

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

    // 비밀번호 확인 메서드 추가
    boolean verifyPassword(String userId, String password);

    // 내가 쓴 댓글 조회
    List<RecipeComment> getMyComments(int page, int pageSize, int memberId);

    // 댓글 삭제
    void deleteComment(String commentId);

    // 댓글 검색
    List<RecipeComment> searchComments(String keyword, int page, int pageSize);

    // 댓글 필터링
    List<RecipeComment> filterComments(String category, String dateRange, int page, int pageSize);

}
