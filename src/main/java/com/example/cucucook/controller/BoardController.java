package com.example.cucucook.controller;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.cucucook.common.ApiResponse;
import com.example.cucucook.domain.Board;
import com.example.cucucook.domain.BoardCategory;
import com.example.cucucook.service.BoardService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/board")
public class BoardController {

  @Autowired
  private BoardService boardService;

  //*********게시판 */
  // 게시판 목록 조회
  @GetMapping(value = "/getBoardList")
  public ApiResponse<List<Board>> getBoardList(@RequestParam String division, @RequestParam String search, @RequestParam String searchType, @RequestParam String boardCategoryId,
      @RequestParam(value = "start", required = false, defaultValue="1") int start, @RequestParam(value = "display", required = true, defaultValue = "20") int display) {
    return boardService.getBoardList(division, search, searchType, boardCategoryId, start, display);
  }

  //게시판 글 상세 조회
  @GetMapping(value = "/getBoard")
  public HashMap<String, Object> getBoard(@RequestParam String boardId) {
    return boardService.getBoard(boardId);
  }

  //게시판 글 등록
  @PostMapping(value = "/insertBoard")
  public HashMap<String, Object> insertBoard(@RequestBody Board board) {
    System.out.println("Board Object: " + board);
      
    return boardService.insertBoard(board);
  }

  //게시판 글 수정
  @PutMapping(value = "/updateBoard")
  public HashMap<String, Object> updateBoard(@RequestParam String boardId, @RequestBody Board board) {
    return boardService.updateBoard(boardId, board);
  }

  //게시판 글 삭제
  @DeleteMapping(value = "/deleteBoard")
  public HashMap<String, Object> deleteBoard(@RequestParam String boardId) {
    return boardService.deleteBoard(boardId);
  }


  // ****************카테고리
  // 카테고리 목록 조회
  @GetMapping(value = "/getBoardCategoryList")
  public ApiResponse<List<BoardCategory>> getBoardCategoryList( 
      @RequestParam(value = "start", required = false, defaultValue="1") int start, @RequestParam(value = "display", required = true, defaultValue = "20") int display) {
    return boardService.getBoardCategoryList(start, display);
  }

  //카테고리 상세 조회
  @GetMapping(value = "/getBoardCategory")
  public HashMap<String, Object> getBoardCategory(@RequestParam String boardCategoryId) {
    return boardService.getBoardCategory(boardCategoryId);
  }

  //카테고리 등록
  @PostMapping(value = "/insertBoardCategory")
  public HashMap<String, Object> insertBoardCategory(@RequestBody BoardCategory boardCategory) {
    return boardService.insertBoardCategory(boardCategory);
  }
  

}
