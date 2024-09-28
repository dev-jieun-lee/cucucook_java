package com.example.cucucook.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.cucucook.common.ApiResponse;
import com.example.cucucook.domain.Board;
import com.example.cucucook.domain.BoardCategory;
import com.example.cucucook.mapper.BoardMapper;
import com.example.cucucook.service.BoardService;

@Service
public class BoardServiceImpl implements BoardService {

  @Autowired
  private BoardMapper boardMapper;

  // 게시판 목록 조회
  @Override
  public ApiResponse<List<Board>> getBoardList(String division, String search, String searchType,
      String boardCategoryId, int start, int display) {
    start = start > 0 ? start : 1;
    display = display > 0 ? display : 10;

    List<Board> boardList = boardMapper.getBoardList(division, search, searchType, boardCategoryId, start, display);
    String message = (boardList == null || boardList.isEmpty()) ? "게시판 목록이 없습니다." : "게시판 목록 조회 성공";
    boolean success = boardList != null && !boardList.isEmpty();
    return new ApiResponse<>(success, message, boardList, null);
  }

  // 게시판 상세 조회
  @Override
  public HashMap<String, Object> getBoard(String boardId) {
    HashMap<String, Object> result = new HashMap<>();

    Board board = boardMapper.getBoard(boardId);

    if (board == null) {
      result.put("success", false);
      result.put("message", "게시물이 존재하지 않습니다.");
      return result;
    } else {

      // 조회 성공 후 viewCount 증가
      boardMapper.updateViewCount(boardId);

      result.put("success", true);
      result.put("message", "게시물 조회 성공.");
      result.put("data", board);
    }

    return result;
  }

  // 게시판 답글 포함 상세 조회
  @Override
  public HashMap<String, Object> getBoardWithReplies(String boardId) {
    HashMap<String, Object> result = new HashMap<>();

    // 부모글과 답글을 모두 조회
    List<Board> boardList = boardMapper.getBoardWithReplies(boardId);

    if (boardList == null || boardList.isEmpty()) {
      result.put("success", false);
      result.put("message", "게시물이 존재하지 않습니다.");
      return result;
    } else {
      // 첫 번째 게시물이 부모글인지 확인 (첫 번째 항목이 부모글이어야 함)
      Board parentBoard = boardList.stream()
          .filter(board -> board.getStatus().equals("0"))
          .findFirst()
          .orElse(null);

      if (parentBoard == null) {
        result.put("success", false);
        result.put("message", "부모 게시물이 존재하지 않습니다.");
        return result;
      }

      // 조회 성공 후 viewCount 증가
      boardMapper.updateViewCount(boardId);

      result.put("success", true);
      result.put("message", "게시물 조회 성공.");
      result.put("data", boardList); // 부모글과 답글 리스트를 함께 반환
    }

    return result;
  }

  // 게시판 글 등록
  @Override
  public HashMap<String, Object> insertBoard(Board board) {
    HashMap<String, Object> result = new HashMap<>();

    // UUID 생성 및 설정
    String uuid = UUID.randomUUID().toString();
    board.setBoardId(uuid);

    // 현재 시간 설정
    LocalDateTime now = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    String formattedNow = now.format(formatter);

    // 등록 및 수정 시간 설정
    board.setRegDt(formattedNow);
    board.setUdtDt(formattedNow);

    // 실제 데이터베이스에 insert 처리 호출
    int rowsAffected = boardMapper.insertBoard(board);

    // 성공 여부에 따른 결과 처리
    if (rowsAffected > 0) {
      result.put("success", true);
      result.put("message", "게시물이 성공적으로 등록되었습니다.");
    } else {
      result.put("success", false);
      result.put("message", "게시물 등록에 실패했습니다.");
    }
    return result;
  }

  // 게시판 글 수정
  @Override
  public HashMap<String, Object> updateBoard(String boardId, Board board) {
    HashMap<String, Object> result = new HashMap<>();

    // 게시판 아이디 설정
    board.setBoardId(boardId);

    // 현재 시간 설정
    LocalDateTime now = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    String formattedNow = now.format(formatter);

    // 수정 시간 설정
    board.setUdtDt(formattedNow);

    // 게시글 수정
    int updateCount = boardMapper.updateBoard(board);

    // 수정이 성공했는지 여부 체크
    if (updateCount > 0) {
      result.put("success", true);
      result.put("message", "게시글이 성공적으로 수정되었습니다.");
    } else {
      result.put("success", false);
      result.put("message", "해당 게시글을 수정할 수 없습니다. 게시글 ID 또는 작성자 ID를 확인하세요.");
    }
    return result;
  }

  // 게시글 삭제
  @Override
  public HashMap<String, Object> deleteBoard(String boardId) {
    HashMap<String, Object> result = new HashMap<>();

    // 게시글 삭제
    int updateCount = boardMapper.deleteBoard(boardId);

    // 성공했는지 여부 체크
    if (updateCount > 0) {
      result.put("success", true);
      result.put("message", "게시글이 성공적으로 삭제되었습니다.");
    } else {
      result.put("success", false);
      result.put("message", "해당 게시글을 삭제할 수 없습니다. 게시글 ID 또는 작성자 ID를 확인하세요.");
    }
    return result;
  }

  // 카테고리 목록 조회
  @Override
  public ApiResponse<List<BoardCategory>> getBoardCategoryList(int start, int display, String search,
      String searchType) {
    start = start > 0 ? start : 1;
    display = display > 0 ? display : 10;

    List<BoardCategory> boardCategoryList = boardMapper.getBoardCategoryList(start, display, search, searchType);
    String message = (boardCategoryList == null || boardCategoryList.isEmpty()) ? "카테고리 목록이 없습니다." : "카테고리 목록 조회 성공";
    boolean success = boardCategoryList != null && !boardCategoryList.isEmpty();
    return new ApiResponse<>(success, message, boardCategoryList, null);
  }

  // 카테고리 상세 조회
  @Override
  public HashMap<String, Object> getBoardCategory(String boardCategoryId) {
    HashMap<String, Object> result = new HashMap<>();

    BoardCategory boardCategory = boardMapper.getBoardCategory(boardCategoryId);

    if (boardCategory == null) {
      result.put("success", false);
      result.put("message", "카테고리가 존재하지 않습니다.");
      return result;
    } else {
      result.put("success", true);
      result.put("message", "카테고리 조회 성공.");
      result.put("data", boardCategory);
    }

    return result;
  }

  // 카테고리 등록
  @Override
  public HashMap<String, Object> insertBoardCategory(BoardCategory boardCategory) {
    HashMap<String, Object> result = new HashMap<>();

    // UUID 생성 및 설정
    String uuid = UUID.randomUUID().toString();
    boardCategory.setBoardCategoryId(uuid);

    // 현재 시간 설정
    LocalDateTime now = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    String formattedNow = now.format(formatter);

    // 등록 및 수정 시간 설정
    boardCategory.setRegDt(formattedNow);
    boardCategory.setUdtDt(formattedNow);

    // 실제 데이터베이스에 insert 처리 호출
    int rowsAffected = boardMapper.insertBoardCategory(boardCategory);

    // 성공 여부에 따른 결과 처리
    if (rowsAffected > 0) {
      result.put("success", true);
      result.put("message", "카테고리가 성공적으로 등록되었습니다.");
    } else {
      result.put("success", false);
      result.put("message", "카테고리 등록에 실패했습니다.");
    }

    return result;
  }

  @Override
  public HashMap<String, Object> deleteBoardCategory(String boardCategoryId) {
    HashMap<String, Object> result = new HashMap<>();

    // 해당 카테고리를 사용하는 게시글이 있는지 확인
    int count = boardMapper.countByBoardCategoryId(boardCategoryId);

    if (count > 0) {
      // 카테고리를 사용하는 게시글이 있는 경우
      result.put("success", false);
      result.put("message", "해당 카테고리를 사용하는 게시글이 있어 삭제할 수 없습니다.");
      return result;
    }

    // 카테고리를 사용하는 게시글이 없는 경우 삭제
    int updateCount = boardMapper.deleteBoardCategory(boardCategoryId);

    if (updateCount > 0) {
      result.put("success", true);
      result.put("message", "카테고리가 성공적으로 삭제되었습니다.");
    } else {
      result.put("success", false);
      result.put("message", "해당 카테고리를 삭제할 수 없습니다.");
    }
    return result;
  }

}