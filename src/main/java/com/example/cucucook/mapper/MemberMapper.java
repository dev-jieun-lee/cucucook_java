package com.example.cucucook.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.example.cucucook.domain.Board;
import com.example.cucucook.domain.Member;
import com.example.cucucook.domain.RecipeComment;
import com.example.cucucook.domain.RecipeLike;

public interface MemberMapper {

    // 회원 수
    int getMemberCount(@Param("search") String search);

    // 회원 목록 (search: 검색어, start: 페이지 번호, display: 한 페이지에 불러올 갯수)
    List<Member> getMemberList(@Param("search") String search, @Param("start") int start, @Param("display") int display);

    // 회원 보기
    Member getMember(@Param("memberId") int memberId);

    // 회원 등록
    void insertMember(@Param("member") Member member);

    // 회원 수정
    void updateMember(@Param("member") Member member);

    // 회원 삭제
    void deleteMember(@Param("memberId") int memberId);

    // 비밀번호 변경
    void updateMemberPassword(@Param("member") Member member);

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
