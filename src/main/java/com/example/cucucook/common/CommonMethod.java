package com.example.cucucook.common;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class CommonMethod {
  // json 유효성검사
  public static boolean isValidJson(String jsonString) {
    try {
      Gson gson = new Gson();
      gson.fromJson(jsonString, Object.class);
      return true;
    } catch (JsonSyntaxException e) {
      // JSON 형식이 올바르지 않음
      return false;
    }
  }

}
