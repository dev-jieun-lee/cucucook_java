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

    //게시판 카테고리 아이디
    private String boardCategoryId;

    //카테고리명
    private String name;

    //카테고리구분 (QNA, FAQ, BOARD)
    private String division;
}
