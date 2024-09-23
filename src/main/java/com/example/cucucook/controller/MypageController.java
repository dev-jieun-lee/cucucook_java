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
import com.example.cucucook.domain.RecipeComment;
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
    public ResponseEntity<List<RecipeComment>> getMyComments(@RequestParam int page, @RequestParam int pageSize,
            @RequestParam int memberId, @RequestParam(required = false, defaultValue = "comment") String sortOption,
            @RequestParam(required = false, defaultValue = "DESC") String sortDirection) {
        // logger.info("가져온 memberId 확인: {}, 정렬 옵션: {}, 정렬 방향: {}", memberId,
        // sortOption, sortDirection);
        try {
            List<RecipeComment> comments = mypageService.getMyComments(page, pageSize, memberId, sortOption,
                    sortDirection);
            // logger.info("컨트롤러에서 받은 댓글 개수: {}", comments.size());
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

    @PutMapping("/updateMember")
    public ResponseEntity<?> updateMemberInfo(@RequestBody Member member) {
        try {
            mypageService.updateMemberInfo(member);
            return ResponseEntity.ok("회원 정보가 성공적으로 업데이트되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("회원 정보 업데이트 실패: " + e.getMessage());
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

}
