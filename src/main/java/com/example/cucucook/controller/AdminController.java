package com.example.cucucook.controller;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.cucucook.common.ApiResponse;
import com.example.cucucook.domain.BoardCategory;
import com.example.cucucook.domain.Member;
import com.example.cucucook.domain.MemberResponse;
import com.example.cucucook.domain.RecipeCategory;
import com.example.cucucook.service.BoardService;
import com.example.cucucook.service.MemberService;
import com.example.cucucook.service.MypageService;
import com.example.cucucook.service.RecipeService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("api/admin")
public class AdminController {

  @Autowired
  private BoardService boardService;
  @Autowired
  private RecipeService recipeService;
  @Autowired
  private MypageService mypageService;
  @Autowired
  private MemberService memberService;

  // 회원 정보 업데이트
  @PutMapping("/updateMember")
  public ResponseEntity<?> updateMemberInfo(@RequestBody Member member) {
    try {
      // 서비스 호출하여 회원 정보 업데이트
      mypageService.updateMemberInfo(member);
      return ResponseEntity.ok(member);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("회원 정보 업데이트 실패: " + e.getMessage() + ". 입력된 데이터: memberId=" +
              member.getMemberId() + ", name=" + member.getName() + ", email=" +
              member.getEmail() + ", phone=" + member.getPhone());
    }
  }

  @GetMapping("/getMember")
  public ResponseEntity<MemberResponse> getMember(@RequestParam int memberId) {

    try {
      Member foundMember = memberService.getMember(memberId);

      if (foundMember != null && foundMember.getUserId() != null) {
        MemberResponse response = new MemberResponse(
            foundMember.getMemberId(),
            foundMember.getUserId(),
            foundMember.getName(),
            foundMember.getPhone(),
            foundMember.getEmail(),
            foundMember.getRole(),
            foundMember.isSmsNoti(),
            foundMember.isEmailNoti());
        return ResponseEntity.ok().body(response);
      } else {
        return ResponseEntity.status(404).build();
      }
    } catch (Exception e) {
      return ResponseEntity.status(500).body(null);
    }
  }

  // 회원탈퇴
  @DeleteMapping("/deleteAccount/{memberId}")
  public ResponseEntity<String> deleteAccount(@PathVariable int memberId) {
    try {
      memberService.deleteMember(memberId);
      return ResponseEntity.ok("회원 탈퇴 성공");
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("회원 탈퇴 실패: " + e.getMessage());
    }
  }

  // ****************카테고리
  // 카테고리 목록 조회
  @GetMapping(value = "/getBoardCategoryList")
  public ApiResponse<List<BoardCategory>> getBoardCategoryList(
      @RequestParam(value = "start", required = false, defaultValue = "1") int start,
      @RequestParam String search,
      @RequestParam String searchType,
      @RequestParam(value = "display", required = true, defaultValue = "20") int display) {
    return boardService.getBoardCategoryList(start, display, search, searchType);
  }

  // 카테고리 상세 조회
  @GetMapping(value = "/getBoardCategory")
  public HashMap<String, Object> getBoardCategory(@RequestParam String boardCategoryId) {
    return boardService.getBoardCategory(boardCategoryId);
  }

  // 카테고리 등록
  @PostMapping(value = "/insertBoardCategory")
  public HashMap<String, Object> insertBoardCategory(@RequestBody BoardCategory boardCategory) {
    return boardService.insertBoardCategory(boardCategory);
  }

  // 카테고리 수정
  @PutMapping(value = "/updateBoardCategory")
  public HashMap<String, Object> updateBoardCategory(@RequestParam String boardCategoryId,
      @RequestBody BoardCategory boardCategory) {
    return boardService.updateBoardCategory(boardCategoryId, boardCategory);
  }

  // 카테고리 삭제
  @DeleteMapping(value = "/deleteBoardCategory")
  public ResponseEntity<HashMap<String, Object>> deleteBoardCategory(@RequestParam String boardCategoryId) {
    return boardService.deleteBoardCategory(boardCategoryId);
  }

  // 레시피 카테고리 목록 가져오기
  @GetMapping(value = "/getRecipeCategoryList")
  public ApiResponse<List<RecipeCategory>> getRecipeCategoryList(@RequestParam String search,
      @RequestParam(value = "start", defaultValue = "0") int start,
      @RequestParam(value = "display", defaultValue = "10") int display,
      @RequestParam String searchType) {
    return recipeService.getRecipeCategoryList(search, start, display, searchType);
  }

  // 레시피 카테고리 가져오기
  @GetMapping(value = "/getRecipeCategory")
  public ApiResponse<RecipeCategory> getRecipeCategory(@RequestParam String recipeCategoryId) {
    return recipeService.getRecipeCategory(recipeCategoryId);
  }

  @PostMapping(value = "/insertRecipeCategory")
  public ApiResponse<Integer> insertRecipeCategory(@RequestBody RecipeCategory recipeCategory) throws Exception {
    return recipeService.insertRecipeCategory(recipeCategory);
  }

  @PutMapping(value = "/updateRecipeCategory")
  public ApiResponse<Integer> updateRecipeCategory(@RequestParam String recipeCategoryId,
      @RequestBody RecipeCategory recipeCategory) throws Exception {
    return recipeService.updateRecipeCategory(recipeCategoryId, recipeCategory);
  }

  // 레시피 카테고리 목록 삭제
  @DeleteMapping(value = "/deleteRecipeCategory")
  public ApiResponse<Integer> deleteRecipeCategory(@RequestParam String recipeCategoryId) throws Exception {
    return recipeService.deleteRecipeCategory(recipeCategoryId);
  }

}
