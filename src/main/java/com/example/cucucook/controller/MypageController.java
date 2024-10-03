package com.example.cucucook.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.cucucook.domain.Board;
import com.example.cucucook.domain.Member;
import com.example.cucucook.domain.MemberRecipe;
import com.example.cucucook.domain.RecipeComment;
import com.example.cucucook.domain.RecipeLike;
import com.example.cucucook.service.MypageService;

@RestController
@RequestMapping("/api/mypage")
public class MypageController {

    private static final Logger logger = LoggerFactory.getLogger(MypageController.class);

    @Autowired
    private MypageService mypageService;

    @PostMapping("/verify-password")
    public ResponseEntity<Map<String, Object>> verifyPassword(@RequestBody Member member) {
        boolean isVerified = mypageService.verifyPassword(member.getUserId(), member.getPassword());

        Map<String, Object> response = new HashMap<>();
        if (!isVerified) {
            response.put("success", false);
            response.put("message", "회원 정보가 없거나 비밀번호가 일치하지 않습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        response.put("success", true);
        response.put("message", "비밀번호가 확인되었습니다.");
        return ResponseEntity.ok(response);
    }

    ///////////////////// 댓글
    // 내가 쓴 댓글 목록 가져오기
    @GetMapping("/getMyComments")
    public ResponseEntity<List<RecipeComment>> getMyComments(
            @RequestParam int page,
            @RequestParam int pageSize,
            @RequestParam int memberId,
            @RequestParam(required = false, defaultValue = "reg_dt") String sortOption,
            @RequestParam(required = false, defaultValue = "DESC") String sortDirection) {
        logger.info("가져온 memberId 확인: {}, 정렬 옵션: {}, 정렬 방향: {}", memberId,
                sortOption, sortDirection);
        try {
            List<RecipeComment> comments = mypageService.getMyComments(page, pageSize, memberId, sortOption,
                    sortDirection);
            logger.info("컨트롤러에서 받은 댓글 개수: {}", comments.size());
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            logger.error("컨트롤러 댓글 목록 조회 실패: 페이지 {}, 페이지 크기 {}, 정렬 옵션: {}, 정렬 방향: {}", page, pageSize, sortOption,
                    sortDirection, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // 댓글 삭제
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteComment(
            @RequestParam String memberId,
            @RequestParam String commentId,
            @RequestParam(required = false) String pcommentId) {
        // logger.info("가져온 memberId 확인: {}, commentId 확인: {}, pcommentId 확인: {}",
        // memberId, commentId, pcommentId);

        try {
            // 대댓글 여부 확인
            if (pcommentId != null && !pcommentId.isEmpty()) {
                logger.error("댓글 삭제 실패: 대댓글이 존재합니다. 댓글 ID {}", commentId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("대댓글이 있어 댓글을 삭제할 수 없습니다.");
            }

            // 댓글 삭제 로직 호출
            logger.info("컨트롤러 댓글 삭제 진입: memberId {}, commentId {}, pcommentId {}", memberId, commentId, pcommentId);
            mypageService.deleteComment(memberId, commentId); // memberId도 서비스에 전달
            logger.info("댓글 삭제 성공:  회원ID {}, 댓글 ID {}", memberId, commentId);
            return ResponseEntity.ok("댓글이 삭제되었습니다.");
        } catch (Exception e) {
            logger.error("댓글 삭제 실패: 회원 ID {}, 댓글 ID {}, 오류: {}", memberId, commentId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("댓글 삭제 실패");
        }
    }

    // 댓글 검색
    @GetMapping("/search")
    public ResponseEntity<List<RecipeComment>> searchComments(
            @RequestParam String searchKeyword,
            @RequestParam String searchType,
            @RequestParam int memberId,
            @RequestParam int page,
            @RequestParam int pageSize,
            @RequestParam String sortOption,
            @RequestParam String sortDirection) {

        logger.info(
                "댓글 검색 요청: keyword={}, searchType={}, page={}, pageSize={}, sortOption={}, sortDirection={}, memberId={}",
                searchKeyword, searchType, page, pageSize, sortOption, sortDirection);

        try {
            List<RecipeComment> comments = mypageService.searchComments(searchKeyword, searchType, memberId, page,
                    pageSize,
                    sortOption, sortDirection);
            logger.info("댓글 검색 성공, 결과 수: {}", comments.size());
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            logger.error("댓글 검색 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    ///////////////////// 게시글
    // 내가 쓴 게시글 목록 가져오기
    @GetMapping("/getMyBoards")
    public ResponseEntity<List<Board>> getMyBoards(@RequestParam int page, @RequestParam int pageSize,
            @RequestParam int memberId, @RequestParam String boardDivision) {
        logger.info("가져온 memberId 확인: {}", memberId);
        logger.info("boardDivision: {}", boardDivision);
        try {
            List<Board> boards = mypageService.getMyBoards(memberId, page, pageSize, boardDivision);
            logger.info("컨트롤러에서 받은 게시물 개수: {},페이지 {}, 페이지 크기 {}, boardDivision{}", boards.size(), page, pageSize,
                    boardDivision);
            return ResponseEntity.ok(boards);
        } catch (Exception e) {
            logger.error("컨트롤러 게시물 목록 조회 실패: 페이지 {}, 페이지 크기 {}", page, pageSize);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // 회원 정보 업데이트
    @PutMapping("/updateMember")
    public ResponseEntity<?> updateMemberInfo(@RequestBody Member member) {
        Logger logger = LoggerFactory.getLogger(MypageController.class);
        try {
            // 로그: 회원 정보 업데이트 요청 데이터 출력
            logger.info("회원 정보 업데이트 요청: memberId={}, name={}, email={}, phone={}",
                    member.getMemberId(), member.getName(), member.getEmail(), member.getPhone());

            // 서비스 호출하여 회원 정보 업데이트
            mypageService.updateMemberInfo(member);

            // 로그: 성공 시 메시지 출력
            logger.info("회원 정보가 성공적으로 업데이트되었습니다. memberId={}", member.getMemberId());
            return ResponseEntity.ok(member);
        } catch (Exception e) {
            // 로그: 실패 시 에러 및 입력된 데이터 출력
            logger.error("회원 정보 업데이트 실패: memberId={}, name={}, email={}, phone={}, 오류={}",
                    member.getMemberId(), member.getName(), member.getEmail(), member.getPhone(), e.getMessage(), e);

            // 예외 발생 시 디버깅을 위해 더 많은 정보를 반환
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("회원 정보 업데이트 실패: " + e.getMessage() + ". 입력된 데이터: memberId=" +
                            member.getMemberId() + ", name=" + member.getName() + ", email=" +
                            member.getEmail() + ", phone=" + member.getPhone());
        }
    }

    // 비밀번호 변경 API
    @PostMapping("/ChangePasswordByUserAccordion")
    public ResponseEntity<?> changePasswordByUser(@RequestBody Map<String, Object> requestData) {
        try {
            // memberId를 String으로 받고, 이를 Integer로 변환
            String memberIdStr = (String) requestData.get("memberId");
            int memberId = Integer.parseInt(memberIdStr); // String을 Integer로 변환

            String newPassword = (String) requestData.get("newPassword");

            // 비밀번호 변경 로직
            mypageService.changePasswordByUser(memberId, newPassword);

            return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("잘못된 memberId 형식입니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("비밀번호 변경 실패: " + e.getMessage());
        }
    }

    // 회원 활동 통계 정보 가져오기
    @GetMapping("/getActivityStats")
    public ResponseEntity<?> getActivityStats(@RequestParam int memberId) {
        try {
            Map<String, Integer> stats = mypageService.getActivityStats(memberId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("활동 통계를 가져오는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 회원이 쓴 글 목록 (최신 5개 또는 페이지네이션 지원)
    @GetMapping("/getMemberBoardList")
    public ResponseEntity<List<Board>> getMemberBoardList(
            @RequestParam int memberId,
            @RequestParam(defaultValue = "0") int start,
            @RequestParam(defaultValue = "5") int limit) {

        // 로그로 컨트롤러 진입 확인
        logger.info("Controller: Received request to getMemberBoardList with memberId: {}, limit: {}", memberId, limit);

        logger.info("Received request to fetch member board list. memberId: {}, start: {}, limit: {}", memberId, start,
                limit);

        try {
            List<Board> boards = mypageService.getMemberBoardList(memberId, start, limit);
            logger.info("Returning {} boards for memberId: {}", boards.size(), memberId);
            return ResponseEntity.ok(boards);
        } catch (Exception e) {
            logger.error("Error fetching member board list for memberId: {}", memberId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // 레시피목록
    @GetMapping("/getMemberRecipeList")
    public ResponseEntity<List<MemberRecipe>> getMemberRecipeList(
            @RequestParam int memberId,
            @RequestParam(defaultValue = "0") int start,
            @RequestParam(defaultValue = "5") int limit) {
        // 로그로 컨트롤러 진입 확인
        logger.info("Received request to fetch member board list. memberId: {}, start: {}, limit: {}", memberId, start,
                limit);

        try {
            List<MemberRecipe> recipes = mypageService.getMemberRecipeList(memberId, start, limit);
            logger.info("Returning {} boards for memberId: {}", recipes.size(), memberId);
            return ResponseEntity.ok(recipes);
        } catch (Exception e) {
            logger.error("Error fetching member board list for memberId: {}", memberId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/getLikedRecipes")
    public ResponseEntity<?> getLikedRecipes(@RequestParam("memberId") int memberId,
            @RequestParam(defaultValue = "0") int start,
            @RequestParam(defaultValue = "5") int limit) {
        try {
            List<RecipeLike> likedRecipes = mypageService.getRecipeLikeList(memberId, start, limit);
            return ResponseEntity.ok(likedRecipes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving liked recipes");
        }
    }

    // 찜 진입 시 정보 가져오기
    @GetMapping("/getRecipeLikeListOtherInfo")
    public List<MemberRecipe> getRecipeLikeListOtherInfo(@RequestParam("memberId") int memberId,
            @RequestParam(required = false) String recipeCategoryId,
            @RequestParam(required = false) String orderby,
            @RequestParam(defaultValue = "10") int display,
            @RequestParam(defaultValue = "0") int start) {
        // 파라미터 로그 출력
        logger.info(
                "Fetching liked recipes with params: memberId={}, recipeCategoryId={}, orderby={}, display={}, start={}",
                memberId, recipeCategoryId, orderby, display, start);

        return mypageService.getRecipeLikeListOtherInfo(memberId, recipeCategoryId, orderby, display, start);
    }
}
