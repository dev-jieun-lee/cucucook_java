package com.example.cucucook.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
            @RequestParam int memberId, @RequestParam(required = false, defaultValue = "comment") String sortOption,
            @RequestParam(required = false, defaultValue = "DESC") String sortDirection) {
        logger.info("가져온 memberId 확인: {}, 정렬 옵션: {}, 정렬 방향: {}", memberId, sortOption, sortDirection);
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
        logger.info("가져온 memberId 확인: {}, commentId 확인: {}, pcommentId 확인: {}", memberId, commentId, pcommentId);

        try {
            // 대댓글 여부 확인
            if (pcommentId != null && !pcommentId.isEmpty()) {
                logger.error("댓글 삭제 실패: 대댓글이 존재합니다. 댓글 ID {}", commentId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("대댓글이 있어 댓글을 삭제할 수 없습니다.");
            }

            // 댓글 삭제 로직 호출
            logger.info("컨트롤러 댓글 삭제 진입: memberId {}, commentId {}, pcommentId {}", memberId, commentId, pcommentId);
            mypageService.deleteComment(memberId, commentId); // memberId도 서비스에 전달
            logger.info("댓글 삭제 성공: 댓글 ID {}, 회원 ID {}", memberId, commentId);
            return ResponseEntity.ok("댓글이 삭제되었습니다.");
        } catch (Exception e) {
            logger.error("댓글 삭제 실패: 댓글 ID {}, 회원 ID {}, 오류: {}", memberId, commentId, e.getMessage(), e);
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

}
