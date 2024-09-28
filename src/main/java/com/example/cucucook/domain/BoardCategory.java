package com.example.cucucook.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BoardCategory {

    // 게시판 카테고리 아이디
    private String boardCategoryId;

    // 카테고리명 (한글)
    private String name;

    // 카테고리명 (영문)
    private String nameEn;

    // 카테고리구분 (QNA, FAQ, NOTICE)
    private String division;

    // 카테고리 색깔
    private String color;

    // 등록일
    private String regDt;

    // 수정일
    private String udtDt;

}
