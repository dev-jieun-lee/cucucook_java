package com.example.cucucook.service;

import java.util.HashMap;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.example.cucucook.common.ApiResponse;
import com.example.cucucook.domain.Board;
import com.example.cucucook.domain.BoardCategory;
import com.example.cucucook.domain.BoardFiles;

public interface BoardService {

  // 게시판 목록
  public ApiResponse<List<Board>> getBoardList(String division, String search, String searchType,
      String boardCategoryId, int start, int display);

  // 게시판 상세
  public HashMap<String, Object> getBoard(String boardId);

  // 게시판 상세
  public HashMap<String, Object> getBoardWithReplies(String boardId);

  // 게시판 등록
  public HashMap<String, Object> insertBoard(Board board, List<MultipartFile> uploadFileList);

  // 게시판 수정
  public HashMap<String, Object> updateBoard(String boardId, Board board, List<MultipartFile> uploadFileList);

  // 게시판 삭제
  public HashMap<String, Object> deleteBoard(String boardId);

  // 카테고리 목록
  public ApiResponse<List<BoardCategory>> getBoardCategoryList(int start, int display, String search,
      String searchType);

  // 카테고리 상세
  public HashMap<String, Object> getBoardCategory(String boardCategoryId);

  // 카테고리 등록
  public HashMap<String, Object> insertBoardCategory(BoardCategory boardCategory);

  // 카테고리 수정
  public HashMap<String, Object> updateBoardCategory(String boardCategoryId, BoardCategory boardCategory);

  // 카테고리 삭제
  public ResponseEntity<HashMap<String, Object>> deleteBoardCategory(String boardCategoryId);

  // 첨부파일 목록 가져오기
  public ApiResponse<List<BoardFiles>> getBoardFilesList(String boardId);

}
