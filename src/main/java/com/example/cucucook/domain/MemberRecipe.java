package com.example.cucucook.domain;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MemberRecipe implements Serializable {

    private static final long serialVersionUID = 1L;

    // 레시피아이디
    private String recipeId;

    // 회원고유번호
    private int memberId;

    // 레시피 카테고리 아이디
    private String recipeCategoryId;

    // 제목
    private String title;

    // 조리방법 카테고리 아이디
    private String recipeMethodId;

    // 조리방법
    private String method;

    // 조리방법(영문)
    private String methodEn;

    // 이미지 아이디
    private String imgId;

    // 분량(몇인분)
    private int serving;

    // 레벨 카테고리 아이디
    private String recipeLevelId;

    // 난이도
    private String level;

    // 난이도(영문)
    private String levelEn;

    // 조리시간
    private String time;

    // 열량
    private String calory;

    // 팁
    private String tip;

    // 등록일
    private String regDt;

    // 수정일
    private String uptDt;

    // 조회수
    private int viewCount;

    // 댓글수
    private String commentCount;

    // 댓글평점
    private String commentRate;

    // 좋아요 수
    private int likeCount;

    // 좋아요 여부
    private boolean memberRecipeLike;

    // 게시판 카테고리에서 조인해서 가져온 카테고리명
    private String recipeCategoryName;

    // 게시판 카테고리에서 조인해서 가져온 카테고리명(영문)
    private String recipeCategoryNameEn;

    // 조인해서 가져오는 회원 정보
    private Member member;

    // 조인해서 가져오는 이미지 정보
    private MemberRecipeImages memberRecipeImages;
}
