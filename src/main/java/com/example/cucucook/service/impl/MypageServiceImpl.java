package com.example.cucucook.service.impl;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.cucucook.domain.Board;
import com.example.cucucook.domain.Member;
import com.example.cucucook.domain.RecipeComment;
import com.example.cucucook.domain.RecipeLike;
import com.example.cucucook.mapper.MemberMapper;
import com.example.cucucook.mapper.MypageMapper;
import com.example.cucucook.service.MypageService;

@Service
public class MypageServiceImpl implements MypageService {

    private static final Logger logger = LoggerFactory.getLogger(MypageServiceImpl.class);

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private MypageMapper mypageMapper;

    @Override
    public boolean verifyPassword(String userId, String password) {
        // 사용자 정보 확인
        Member member = memberMapper.findByUserId(userId);
        if (member == null) {
            logger.info("해당 회원을 찾을 수 없습니다: userId={}", userId); // 로그에 사용자 ID 출력
            return false;
        }

        // 비밀번호 검증
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        boolean isMatch = encoder.matches(password, member.getPassword());
        if (!isMatch) {
            logger.info("비밀번호가 일치하지 않습니다: userId={}", userId); // 비밀번호 불일치 로그
        } else {
            logger.info("비밀번호가 일치합니다: userId={}", userId); // 비밀번호 일치 로그
        }
        return isMatch;
    }

    @Override
    public int getMemberBoardCount(Long memberId) {
        // 구현이 필요함
        return 0;
    }

    @Override
    public List<Board> getMemberBoardList(Long memberId) {
        // 구현이 필요함
        return Collections.emptyList();
    }

    @Override
    public int getRecipeCommentCount(Long memberId) {
        // 구현이 필요함
        return 0;
    }

    @Override
    public List<RecipeComment> getRecipeCommentList(Long memberId, int start, int display) {
        // 구현이 필요함
        return Collections.emptyList();
    }

    @Override
    public int getMemberRecipeLikeCount(Long memberId) {
        // 구현이 필요함
        return 0;
    }

    @Override
    public List<RecipeLike> getRecipeLikeList(Long memberId, int start, int display) {
        // 구현이 필요함
        return Collections.emptyList();
    }

    @Override
    public RecipeLike getRecipeLike(Long memberId, String recipeId) {
        // 구현이 필요함
        return new RecipeLike();
    }

    @Override
    public void addRecipeLike(RecipeLike recipeLike) {
        // 구현이 필요함
    }

    @Override
    public void removeRecipeLike(Long memberId, String recipeId) {
        // 구현이 필요함
    }

    ///////// 댓글
    // 내가 쓴 댓글 목록 가져오기
    @Override
    public List<RecipeComment> getMyComments(int page, int pageSize, int memberId) {
        int offset = (page - 1) * pageSize;
        try {
            logger.info("댓글 로딩 시도: 페이지 {}, 페이지 크기 {}", page, pageSize);
            List<RecipeComment> comments = mypageMapper.getMyComments(offset, pageSize, memberId);
            logger.info("서비스임플 댓글 로딩 성공: {} 개의 댓글", comments.size());
            return comments;
        } catch (Exception e) {
            logger.error("서비스임플 댓글 로딩 실패: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public void deleteComment(String commentId) {
        try {
            mypageMapper.deleteCommentById(commentId);
            System.out.println("서비스임플 댓글 삭제 성공: " + commentId);
        } catch (Exception e) {
            System.out.println("서비스임플 댓글 삭제 실패: " + e.getMessage());
        }
    }

    // 댓글 검색

    @Override
    public List<RecipeComment> searchComments(String keyword, int page, int pageSize) {
        try {
            int offset = (page - 1) * pageSize;
            List<RecipeComment> comments = mypageMapper.searchMyComments(keyword, offset, pageSize);
            System.out.println("서비스임플 검색 결과: " + comments.size() + " 개의 댓글");
            return comments;
        } catch (Exception e) {
            System.out.println("서비스임플 검색 실패: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    // 댓글 필터링
    public List<RecipeComment> filterComments(String category, String dateRange, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return mypageMapper.filterMyComments(category, dateRange, dateRange, offset, pageSize);
    }
}
