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

  // 레시피 카테고리 아이디
  private String recipeCategoryId;

  // 레시피 카테고리명
  private String name;

  // 레시피 카테고리영문명
  private String nameEn;

  // 구분(카테고리:C/조리방법:M/난이도:L)
  private String division;

  // 카테고리 별 레시피 수
  private String count;

  // 등록일
  private String regDt;

  // 수정일
  private String uptDt;

}
