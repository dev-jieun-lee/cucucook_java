package com.example.cucucook.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Board {

    // 게시판아이디
    private String boardId;

    // 게시판 카테고리 구분(FAQ, QNA, BOARD)
    private String boardDivision;

    // 회원 고유번호
    private int memberId;

    // 작성자
    private String userName;

    // 제목
    private String title;

    // 내용
    private String contents;

    // 카테고리아이디
    private String boardCategoryId;

    // 등록일
    private String regDt;

    // 수정일
    private String udtDt;

    // 구분 (0:게시글 1:답글)
    private String status;

    // 조회수
    private int viewCount;

    // 부모 게시판 아이디
    private String pboardId;

    // 조인해서 가져오는 회원 정보
    private Member member;

    // 게시판 카테고리에서 조인해서 가져온 카테고리명
    private String boardCategoryName;

    // 답글 갯수
    private int replyCount;

}
