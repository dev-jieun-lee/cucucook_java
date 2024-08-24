package com.example.cucucook.service;

import java.util.HashMap;
import java.util.List;

import com.example.cucucook.common.ApiResponse;
import com.example.cucucook.domain.Board;
import com.example.cucucook.domain.BoardCategory;

public interface BoardService {

  // 게시판 목록
  public ApiResponse<List<Board>> getBoardList(String search, String boardCategoryId, int start, int display);

  // 게시판 상세
  public HashMap<String, Object> getBoard(String boardId);
  
  //게시판 등록
  public HashMap<String, Object> insertBoard(Board board);

  //게시판 수정
  public HashMap<String, Object> updateBoard(String boardId, Board board);

  //게시판 삭제
  public HashMap<String, Object> deleteBoard(String boardId);
  
  // 카테고리 목록
  public ApiResponse<List<BoardCategory>> getBoardCategoryList( int start, int display);
}
