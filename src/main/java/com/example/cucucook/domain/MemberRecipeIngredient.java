package com.example.cucucook.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberRecipeIngredient {

  // 재료 아이디
  private String ingredientId;

  // 순번
  private int orderId;

  // 레시피 아이디
  private String recipeId;

  // 재료명
  private String name;

  // 재료단위
  private String unit;

  // 재료량
  private String amount;
}
