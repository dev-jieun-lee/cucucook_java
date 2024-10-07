package com.example.cucucook.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.cucucook.domain.Board;
import com.example.cucucook.domain.MemberRecipe;
import com.example.cucucook.domain.RecipeComment;
import com.example.cucucook.domain.RecipeLike;

@Mapper
public interface MypageMapper {

        // 회원이 쓴 글 갯수
        int getMemberBoardCount(@Param("memberId") int memberId);

        // 회원이 쓴 글 목록 (최신 5개 또는 페이지네이션 지원)
        List<Board> getMemberBoardList(
                        @Param("memberId") int memberId,
                        @Param("start") Integer start,
                        @Param("display") Integer display);

        // 회원이 쓴 댓글 갯수
        int getRecipeCommentCount(@Param("memberId") int memberId);

        // 회원이 찜한 레시피 갯수
        int getMemberRecipeLikeCount(@Param("memberId") int memberId);

        // 회원 레시피 찜 목록
        List<RecipeLike> getRecipeLikeList(@Param("memberId") int memberId, @Param("start") int start,
                        @Param("display") int display);

        // 회원 레시피 찜 보기
        RecipeLike getRecipeLike(@Param("memberId") int memberId, @Param("recipeId") String recipeId);

        // 댓글//
        // 내가 쓴 댓글 가져오기
        List<RecipeComment> getMyComments(
                        @Param("offset") int offset,
                        @Param("pageSize") int pageSize,
                        @Param("memberId") int memberId,
                        @Param("sortOption") String sortOption,
                        @Param("sortDirection") String sortDirection);

        // 댓글 검색
        List<RecipeComment> searchByContent(@Param("keyword") String keyword,
                        @Param("offset") int offset,
                        @Param("pageSize") int pageSize,
                        @Param("sortOption") String sortOption,
                        @Param("sortDirection") String sortDirection);

        List<RecipeComment> searchByRecipeTitle(@Param("keyword") String keyword,
                        @Param("offset") int offset,
                        @Param("pageSize") int pageSize,
                        @Param("sortOption") String sortOption,
                        @Param("sortDirection") String sortDirection);

        // 댓글 및 레시피 제목 검색
        List<RecipeComment> searchByKeyword(
                        @Param("memberId") int memberId,
                        @Param("keyword") String keyword,
                        @Param("searchType") String searchType,
                        @Param("offset") int offset,
                        @Param("pageSize") int pageSize,
                        @Param("sortOption") String sortOption,
                        @Param("sortDirection") String sortDirection);

        // 게시물//
        // 내가 쓴 게시물 가져오기
        List<Board> getMyBoards(
                        @Param("memberId") int memberId,
                        @Param("offset") int offset,
                        @Param("pageSize") int pageSize,
                        @Param("search") String search,
                        @Param("searchType") String searchType,
                        @Param("boardDivision") String boardDivision);

        // 회원정보 통계 가져오기
        int getLikeCount(int memberId);

        int getWriteCount(int memberId);

        int getReplyCount(int memberId);

        int getRecipeCount(int memberId);

        // 회원 레시피 가져오기
        List<MemberRecipe> getMemberRecipeList(int memberId, int start, int limit);

        // 찜페이지 진입시 정보가져오기
        List<MemberRecipe> getRecipeLikeListOtherInfo(
                        @Param("memberId") int memberId,
                        @Param("recipeCategoryId") String recipeCategoryId,
                        @Param("orderby") String orderby,
                        @Param("display") int display,
                        @Param("start") int start);

}
