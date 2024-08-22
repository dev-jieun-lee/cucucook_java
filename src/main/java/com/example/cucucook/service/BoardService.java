package com.example.cucucook.service;

import java.util.List;

import com.example.cucucook.common.ApiResponse;
import com.example.cucucook.domain.Board;

public interface BoardService {

  // 게시판 목록
  public ApiResponse<List<Board>> getBoardList(String search, String boardCategoryId, int start, int display);

}
