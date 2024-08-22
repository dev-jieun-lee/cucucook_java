package com.example.cucucook.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.cucucook.common.ApiResponse;
import com.example.cucucook.domain.Board;
import com.example.cucucook.service.BoardService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/board")
public class BoardController {

  @Autowired
  private BoardService boardService;

  // 게시판 목록 조회
  @GetMapping(value = "/getBoardList")
  public ApiResponse<List<Board>> getBoardList(@RequestParam String search, @RequestParam String boardCategoryId,
      @RequestParam(value = "start", required = false, defaultValue="1") int start, @RequestParam(value = "display", required = true, defaultValue = "20") int display) {
    return boardService.getBoardList(search, boardCategoryId, start, display);
  }
}
