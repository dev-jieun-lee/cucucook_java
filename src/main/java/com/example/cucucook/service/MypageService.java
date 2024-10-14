package com.example.cucucook.service;

import java.util.List;
import java.util.Map;

import com.example.cucucook.domain.Board;
import com.example.cucucook.domain.Member;
import com.example.cucucook.domain.MemberRecipe;
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
  Map<String, Object> getMyComments(int page, int pageSize, int memberId, String sortOption,
      String sortDirection, String search, String searchType);

  // 댓글 삭제
  void deleteComment(String memberId, String commentId);

  // 내가 쓴 글
  // 내가 쓴 게시글 조회
  Map<String, Object> getMyBoards(int memberId, int page, int pageSize, String boardDivision, String search,
      String searchType);

  // 회원이 쓴 글 목록 (최신 5개 또는 페이지네이션 지원)
  List<Board> getMemberBoardList(int memberId, int start, int limit);

  // 회원 레시피리스트
  List<MemberRecipe> getMemberRecipeList(int memberId, int start, int limit, String search,
      String searchType);

  // 통합된 찜한 레시피 목록 및 검색 기능 메서드
  List<MemberRecipe> getRecipeLikeListOtherInfo(int memberId, String recipeCategoryId, String orderby,
      int display,
      int start);
}