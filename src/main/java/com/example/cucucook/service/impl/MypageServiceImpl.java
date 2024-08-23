package com.example.cucucook.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.cucucook.domain.Board;
import com.example.cucucook.domain.RecipeComment;
import com.example.cucucook.domain.RecipeLike;
import com.example.cucucook.mapper.MypageMapper;
import com.example.cucucook.service.MypageService;

@Service
public class MypageServiceImpl implements MypageService {

    private final MypageMapper mypageMapper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public MypageServiceImpl(MypageMapper mypageMapper, PasswordEncoder passwordEncoder) {
        this.mypageMapper = mypageMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public int getMemberBoardCount(Long memberId) {
        return mypageMapper.getMemberBoardCount(memberId.intValue());
    }

    @Override
    public List<Board> getMemberBoardList(Long memberId) {
        return mypageMapper.getMemberBoardList(memberId.intValue());
    }

    @Override
    public int getRecipeCommentCount(Long memberId) {
        return mypageMapper.getRecipeCommentCount(memberId.intValue());
    }

    @Override
    public List<RecipeComment> getRecipeCommentList(Long memberId, int start, int display) {
        return mypageMapper.getRecipeCommentList(memberId.intValue(), start, display);
    }

    @Override
    public int getMemberRecipeLikeCount(Long memberId) {
        return mypageMapper.getMemberRecipeLikeCount(memberId.intValue());
    }

    @Override
    public List<RecipeLike> getRecipeLikeList(Long memberId, int start, int display) {
        return mypageMapper.getRecipeLikeList(memberId.intValue(), start, display);
    }

    @Override
    public RecipeLike getRecipeLike(Long memberId, String recipeId) {
        return mypageMapper.getRecipeLike(memberId.intValue(), recipeId);
    }

    @Override
    public void addRecipeLike(RecipeLike recipeLike) {
        mypageMapper.insertRecipeLike(recipeLike);
    }

    @Override
    public void removeRecipeLike(Long memberId, String recipeId) {
        mypageMapper.deleteRecipeLike(memberId.intValue(), recipeId);
    }

}
