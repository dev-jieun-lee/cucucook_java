package com.example.cucucook.service.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.cucucook.domain.Board;
import com.example.cucucook.domain.Member;
import com.example.cucucook.domain.MemberRecipe;
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

    // 회원정보 수정
    @Override
    public void updateMemberInfo(Member member) throws Exception {
        // 로그 추가
        logger.info("회원 정보 업데이트 시작: memberId={}, name={}, email={}, phone={}",
                member.getMemberId(), member.getName(), member.getEmail(), member.getPhone());

        try {
            // 회원 정보 업데이트 로직
            memberMapper.updateMemberInfo(member);
            logger.info("회원 정보 업데이트 성공: memberId={}", member.getMemberId());
        } catch (Exception e) {
            logger.error("회원 정보 업데이트 실패: memberId={}, 오류: {}", member.getMemberId(), e.getMessage(), e);
            throw e;
        }
    }

    // 회원 비밀번호 수정
    @Override
    public void changePasswordByUser(int memberId, String newPassword) throws Exception {
        logger.info("비밀번호 변경 요청 수신: memberId={}", memberId);

        // 1. 비밀번호 암호화 시작
        logger.info("비밀번호 암호화 시작: memberId={}", memberId);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encodedPassword = encoder.encode(newPassword);

        // 2. 비밀번호 암호화 완료
        logger.info("비밀번호 암호화 완료: memberId={}, 암호화된 비밀번호={}", memberId, encodedPassword);

        // 3. 데이터베이스에 비밀번호 업데이트 시도
        logger.info("비밀번호 업데이트 시도: memberId={}", memberId);
        try {
            memberMapper.changePasswordByUser(memberId, encodedPassword);
            logger.info("비밀번호 업데이트 성공: memberId={}", memberId);
        } catch (Exception e) {
            logger.error("비밀번호 업데이트 실패: memberId={}", memberId, e);
            throw new Exception("비밀번호 업데이트 중 오류 발생", e);
        }
    }

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
    public int getMemberRecipeLikeCount(Long memberId) {
        // 구현이 필요함
        return 0;
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

    // 회원정보 통계 가져오기
    @Override
    public Map<String, Integer> getActivityStats(int memberId) {
        Map<String, Integer> stats = new HashMap<>();
        try {
            int likeCount = mypageMapper.getLikeCount(memberId);
            int writeCount = mypageMapper.getWriteCount(memberId);
            int replyCount = mypageMapper.getReplyCount(memberId);

            stats.put("likeCount", likeCount);
            stats.put("writeCount", writeCount);
            stats.put("replyCount", replyCount);
        } catch (Exception e) {
            throw new RuntimeException("활동 통계 조회 중 오류가 발생했습니다.", e);
        }
        return stats;
    }

    // 회원이 쓴 글 목록 (최신 5개 또는 페이지네이션 지원)
    @Override
    public List<Board> getMemberBoardList(int memberId, int start, int limit) {
        logger.info("Fetching member board list. memberId: {}, start: {}, limit: {}", memberId, start, limit);

        try {
            List<Board> boards = mypageMapper.getMemberBoardList(memberId, start, limit);
            logger.info("Fetched {} boards for memberId: {}", boards.size(), memberId);
            return boards;
        } catch (Exception e) {
            logger.error("Error fetching member board list for memberId: {}", memberId, e);
            throw e; // 오류를 다시 던져서 컨트롤러에서 처리
        }
    }

    @Override
    public List<MemberRecipe> getMemberRecipeList(int memberId, int start, int limit) {

        logger.info("Fetching member board list. memberId: {}, start: {}, limit: {}", memberId, start, limit);

        try {
            List<MemberRecipe> recipes = mypageMapper.getMemberRecipeList(memberId, start, limit);
            logger.info("Fetched {} recipes for memberId: {}", recipes.size(), memberId);
            return recipes;
        } catch (Exception e) {
            logger.error("Error fetching member recipe list for memberId: {}", memberId, e);
            throw e;
        }
    }

    // 찜
    @Override
    public List<RecipeLike> getRecipeLikeList(int memberId, int start, int limit) {
        return mypageMapper.getRecipeLikeList(memberId, start, limit);
    }

    // 찜 진입시 가져오기
    @Override
    public List<MemberRecipe> getRecipeLikeListOtherInfo(int memberId, String recipeCategoryId,
            String orderby,
            int display, int start) {
        return mypageMapper.getRecipeLikeListOtherInfo(memberId, recipeCategoryId, orderby, display, start);
    }
}
