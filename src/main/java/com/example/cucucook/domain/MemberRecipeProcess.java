package com.example.cucucook.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberRecipeProcess {

  // 레시피과정아이디
  private String recipeProcessId;

  // 레시피아이디
  private String recipeId;

  // 레시피순서
  private int recipeNumber;

  // 이미지 아이디
  private String imgId;

  // 내용
  private String contents;

  // 조인해서 가져오는 이미지 정보
  private MemberRecipeImages memberRecipeImages;
}
