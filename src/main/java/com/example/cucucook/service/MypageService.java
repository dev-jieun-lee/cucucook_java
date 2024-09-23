package com.example.cucucook.service;

import java.util.List;

import com.example.cucucook.domain.Board;
import com.example.cucucook.domain.RecipeComment;
import com.example.cucucook.domain.RecipeLike;

public interface MypageService {

    // 회원 게시물 관련 메서드
    int getMemberBoardCount(Long memberId);

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
    List<RecipeComment> getMyComments(int page, int pageSize, int memberId, String sortOption, String sortDirection);

    // 댓글 삭제
    void deleteComment(String memberId, String commentId);

    // 댓글검색
    List<RecipeComment> searchComments(
            String keyword,
            String searchType,
            int memberId,
            int page,
            int pageSize,
            String sortOption,
            String sortDirection);

    // 내가 쓴 글
    // 내가 쓴 게시글 조회
    List<Board> getMyBoards(int memberId, int page, int pageSize, String boardDivision);

}