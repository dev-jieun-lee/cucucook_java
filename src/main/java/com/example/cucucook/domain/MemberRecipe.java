package com.example.cucucook.domain;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberRecipe implements Serializable {

    private static final long serialVersionUID = 1L;

    //레시피아이디
    private String recipeId;

    //회원고유번호
    private int memberId;

    //레시피 카테고리 아이디
    private String recipeCategoryId;

    //제목
    private String title;

    //조리방법
    private String method;

    //이미지 아이디
    private String imgId;

    //분량(몇인분)
    private int serving;

    //난이도
    private String level;

    //조리시간
    private String time;

    //열량
    private String calory;

    //팁
    private String tip;

    //등록일
    private String regDt;

    //수정일
    private String uptDt;

    //조회수
    private int viewCount;

    //게시판 카테고리에서 조인해서 가져온 카테고리명
    private String recipeCategoryName;

    //조인해서 가져오는 회원 정보
    private Member member;
}
