package com.example.cucucook.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecipeLike {

    //레시피 아이디
    private String recipeId;

    //회원고유번호
    private String memberId;

    //등록일
    private String regDt;

}
