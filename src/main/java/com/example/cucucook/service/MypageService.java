package com.example.cucucook.service;

import java.util.List;
import java.util.Map;

import com.example.cucucook.domain.Board;
import com.example.cucucook.domain.Member;
import com.example.cucucook.domain.MemberRecipe;
import com.example.cucucook.domain.RecipeComment;
import com.example.cucucook.domain.RecipeLike;

public interface MypageService {

        // 회원정보수정
        void updateMemberInfo(Member member) throws Exception;

        // 회원비밀번호수정
        void changePasswordByUser(int memberId, String newPassword) throws Exception;

        // 회원 정보 통계 가져오기
        Map<String, Integer> getActivityStats(int memberId);

        // 회원 게시물 관련 메서드
        int getMemberBoardCount(Long memberId);

        // 회원 댓글 관련 메서드
        int getRecipeCommentCount(Long memberId);

        // 회원 레시피 찜 갯수 관련 메서드
        int getMemberRecipeLikeCount(Long memberId);

        // 회원 찜 리스트
        List<RecipeLike> getRecipeLikeList(int memberId, int start, int display);

        // 비밀번호 확인 메서드 추가
        boolean verifyPassword(String userId, String password);

        // 내가 쓴 댓글 조회
        List<RecipeComment> getMyComments(int page, int pageSize, int memberId, String sortOption,
                        String sortDirection);

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

        // 회원이 쓴 글 목록 (최신 5개 또는 페이지네이션 지원)
        List<Board> getMemberBoardList(int memberId, int start, int limit);

        // 회원 레시피리스트
        List<MemberRecipe> getMemberRecipeList(int memberId, int start, int limit);

        // 찜 진입시 정보가져오기
        List<MemberRecipe> getRecipeLikeListOtherInfo(int memberId, String recipeCategoryId, String orderby,
                        int display,
                        int start);
}