package com.example.cucucook.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecipeCategory {

    //레시피 카테고리 아이디
    private String recipeCategoryId;
    //레시피 카테고리명
    private String name;

}
