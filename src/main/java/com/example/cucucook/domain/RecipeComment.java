package com.example.cucucook.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecipeComment {

    //댓글 아이디
    private String commentId;

    //회원 고유번호
    private int memberId;

    //댓글 내용
    private String comment;

    //레시피 아이디
    private String recipeId;

    //평점
    private int rate;

    //등록일
    private String regDt;

    //수정일
    private String uptDt;

    //상태 (0:댓글 1:답글)
    private String status;

    //부모 댓글 아이디
    private String pCommentId;

    //조인해서 가져오는 회원 정보
    private Member member;

}
