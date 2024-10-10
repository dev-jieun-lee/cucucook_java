package com.example.cucucook.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.cucucook.domain.Board;
import com.example.cucucook.domain.BoardCategory;
import com.example.cucucook.domain.BoardFiles;

@Mapper
public interface BoardMapper {

  // 게시판 글 갯수
  int getBoardCount(@Param("search") String search, @Param("boardCategoryId") String boardCategoryId);

  // 게시판 목록 (search: 검색어, boardCategoryId: 게시판 카테고리 아이디, start: 페이지 번호, display: 한
  // 페이지에 불러올 갯수)
  List<Board> getBoardList(@Param("division") String division, @Param("search") String search,
      @Param("searchType") String searchType, @Param("boardCategoryId") String boardCategoryId,
      @Param("start") Integer start, @Param("display") Integer display);

  // 게시판 상세 보기
  Board getBoard(@Param("boardId") String boardId);

  // 게시판 답글 포함 상세
  public List<Board> getBoardWithReplies(String boardId);

  // 게시판 조회수 증가
  void updateViewCount(@Param("boardId") String boardId);

  // 게시판 작성
  int insertBoard(@Param("board") Board board);

  // 게시판 수정
  int updateBoard(@Param("board") Board board);

  // 게시판 삭제
  int deleteBoard(@Param("boardId") String boardId);

  // 답글 갯수
  int getReplyCount(@Param("pboardId") String pboardId);

  // 답글 목록
  List<Board> getReplyList(@Param("boardId") String boardId, @Param("start") int start,
      @Param("display") int display);

  // 답글 보기
  Board getReply(@Param("boardId") String boardId);

  // 답글 작성
  int insertReply(@Param("board") Board board);

  // 답글 수정
  int updateReply(@Param("board") Board board);

  // 답글 삭제
  int deleteReply(@Param("boardId") String boardId);

  // 게시판 카테고리 갯수
  int getBoardCategoryCount();

  // 게시판 카테고리 목록
  List<BoardCategory> getBoardCategoryList(@Param("start") int start, @Param("display") int display,
      @Param("search") String search, @Param("searchType") String searchType);

  // 게시판 카테고리 보기
  BoardCategory getBoardCategory(@Param("boardCategoryId") String boardCategoryId);

  // 게시판 카테고리 추가
  int insertBoardCategory(@Param("boardCategory") BoardCategory boardCategory);

  // 게시판 카테고리 수정
  int updateBoardCategory(@Param("boardCategory") BoardCategory boardCategory);

  // 게시판 카테고리 삭제
  int deleteBoardCategory(@Param("boardCategoryId") String boardCategoryId);

  // 카테고리 갯수
  int countByBoardCategoryId(@Param("boardCategoryId") String boardCategoryId);

  // 첨부파일 정보 목록
  List<BoardFiles> getBoardFilesList(@Param("boardId") String boardId);

  // 첨부파일 정보 상세
  BoardFiles getBoardFiles(@Param("boardId") String boardId, @Param("fileId") String fileId);

  // 첨부파일 정보 넣기
  int insertBoardFiles(@Param("boardFiles") BoardFiles boardFiles);

  // 첨부파일 정보 수정
  int updateBoardFiles(@Param("boardFiles") BoardFiles boardFiles);

  // 첨부파일 정보 삭제
  int deleteBoardFiles(@Param("boardId") String boardId, @Param("fileId") String fileId);

}
