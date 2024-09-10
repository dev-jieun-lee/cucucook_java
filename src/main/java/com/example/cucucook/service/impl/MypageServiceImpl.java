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
import com.example.cucucook.service.MypageService;

@Service
public class MypageServiceImpl implements MypageService {

    private static final Logger logger = LoggerFactory.getLogger(MypageServiceImpl.class);

    @Autowired
    private MemberMapper memberMapper;

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
}
