package com.example.cucucook.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<String> verifyPassword(@RequestBody Member member) {
        boolean isVerified = mypageService.verifyPassword(member.getUserId(), member.getPassword());

        if (!isVerified) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("회원 정보가 없거나 비밀번호가 일치하지 않습니다.");
        }

        // 비밀번호가 일치하면 200 OK와 함께 메시지 반환
        return ResponseEntity.ok("비밀번호가 확인되었습니다.");
    }

    ///////////////////// 댓글

    // 내가 쓴 댓글 목록 가져오기
    @GetMapping("/getMyComments")
    public ResponseEntity<List<RecipeComment>> getMyComments(@RequestParam int page, @RequestParam int pageSize,
            @RequestParam int memberId) {
        logger.info("가져온 memberId확인", memberId, logger);
        try {
            List<RecipeComment> comments = mypageService.getMyComments(page, pageSize, memberId);
            logger.info("컨트롤러 댓글 목록 조회 성공: 페이지 {}, 페이지 크기 {}, 결과 수 {}, 회원memberId {}", page, pageSize, comments.size(),
                    memberId);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            logger.error("컨트롤러 댓글 목록 조회 실패: 페이지 {}, 페이지 크기 {}", page, pageSize, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // 댓글 삭제
    @DeleteMapping("/{commentId}/delete")
    public ResponseEntity<String> deleteComment(@PathVariable String commentId) {
        try {
            mypageService.deleteComment(commentId);
            logger.info("댓글 삭제 성공: 댓글 ID {}", commentId);
            return ResponseEntity.ok("댓글이 삭제되었습니다.");
        } catch (Exception e) {
            logger.error("댓글 삭제 실패: 댓글 ID {}", commentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("댓글 삭제 실패");
        }
    }

    // 댓글 검색
    @GetMapping("/search")
    public ResponseEntity<List<RecipeComment>> searchComments(@RequestParam String keyword, @RequestParam int page,
            @RequestParam int pageSize) {
        try {
            List<RecipeComment> comments = mypageService.searchComments(keyword, page, pageSize);
            logger.info("댓글 검색 성공: 키워드 '{}', 페이지 {}, 페이지 크기 {}, 결과 수 {}", keyword, page, pageSize, comments.size());
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            logger.error("댓글 검색 실패: 키워드 '{}', 페이지 {}, 페이지 크기 {}", keyword, page, pageSize, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // 댓글 필터링
    @GetMapping("/filter")
    public ResponseEntity<List<RecipeComment>> filterComments(@RequestParam(required = false) String category,
            @RequestParam(required = false) String dateRange, @RequestParam int page, @RequestParam int pageSize) {
        try {
            List<RecipeComment> comments = mypageService.filterComments(category, dateRange, page, pageSize);
            logger.info("댓글 필터링 성공: 카테고리 '{}', 날짜 범위 '{}', 페이지 {}, 페이지 크기 {}, 결과 수 {}", category, dateRange, page,
                    pageSize, comments.size());
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            logger.error("댓글 필터링 실패: 카테고리 '{}', 날짜 범위 '{}', 페이지 {}, 페이지 크기 {}", category, dateRange, page, pageSize, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}
