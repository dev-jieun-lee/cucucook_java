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
    public List<RecipeComment> getMyComments(int page, int pageSize, int memberId, String sortOption,
            String sortDirection) {
        int offset = page > 0 ? (page - 1) * pageSize : 0;
        try {
            // logger.info("댓글 로딩 시도: 페이지 {}, 페이지 크기 {}, 정렬 옵션: {}, 정렬 방향: {}", page,
            // pageSize, sortOption, sortDirection);
            List<RecipeComment> comments = mypageMapper.getMyComments(offset, pageSize, memberId, sortOption,
                    sortDirection);
            return comments;
        } catch (Exception e) {
            logger.error("댓글 로딩 실패: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    // 삭제 로직
    public void deleteComment(String memberId, String commentId) {
        logger.info("서비스에서 댓글 삭제: memberId={}, commentId={}", memberId, commentId);

        try {
            logger.info("댓글 삭제를 위한 매퍼 호출 전: commentId={}, memberId={}", memberId, commentId);
            mypageMapper.deleteComment(Integer.parseInt(memberId), commentId);
            logger.info("서비스에서 댓글 삭제 성공: commentId={}", commentId);
        } catch (Exception e) {
            logger.error("서비스에서 댓글 삭제 실패: commentId={}, 오류={}", commentId, e.getMessage(), e);
        }
    }

    // 댓글 검색
    @Override
    public List<RecipeComment> searchComments(String keyword, String searchType, int memberId, int page, int pageSize,
            String sortOption, String sortDirection) {
        logger.info("댓글 검색 시작: keyword={}, searchType={}, page={}, pageSize={}, sortOption={}, sortDirection={}",
                keyword, searchType, page, pageSize, sortOption, sortDirection);

        try {
            // page가 1보다 작은 경우 1로 설정
            int currentPage = Math.max(page, 1);
            int offset = (currentPage - 1) * pageSize;
            logger.info("계산된 offset: {}", offset);

            List<RecipeComment> comments = mypageMapper.searchByKeyword(
                    memberId, keyword,
                    searchType,
                    offset, pageSize, sortOption,
                    sortDirection);

            logger.info("검색 완료, 결과 수: {}", comments.size());
            return comments;
        } catch (Exception e) {
            logger.error("댓글 검색 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("댓글 검색 실패", e);
        }
    }

    ///////// 게시판
    // 내가 쓴 게시판 목록 가져오기
    @Override
    public List<Board> getMyBoards(int memberId, int page, int pageSize, String boardDivision) {
        int offset = page > 0 ? (page - 1) * pageSize : 0;
        try {
            List<Board> boards = mypageMapper.getMyBoards(memberId, offset, pageSize, boardDivision);
            logger.info("게시물 로딩 성공: 페이지 {}, 페이지 크기 {}, offset {}, 반환된 게시물 수 {}", page, pageSize, offset, boards.size());
            return boards;
        } catch (Exception e) {
            logger.error("게시물 로딩 실패: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

}
