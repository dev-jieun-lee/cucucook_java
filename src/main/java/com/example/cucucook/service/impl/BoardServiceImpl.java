package com.example.cucucook.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.cucucook.common.ApiResponse;
import com.example.cucucook.domain.Board;
import com.example.cucucook.mapper.BoardMapper;
import com.example.cucucook.service.BoardService;

@Service
public class BoardServiceImpl implements BoardService {

  @Autowired
  private BoardMapper boardMapper;

  // 게시판 목록 조회
  @Override
  public ApiResponse<List<Board>> getBoardList(String search, String boardCategoryId, int start, int display) {
    start = start > 0 ? start : 1;
    display = display > 0 ? display : 10;

    List<Board> boardList = boardMapper.getBoardList(search, boardCategoryId, start, display);
    String message = (boardList == null || boardList.isEmpty()) ? "게시판 목록이 없습니다." : "게시판 목록 조회 성공";
    boolean success = boardList != null && !boardList.isEmpty();
    return new ApiResponse<>(success, message, boardList);
  }
}