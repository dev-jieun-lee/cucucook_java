package com.example.cucucook.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BoardFiles {

  // 파일 아이디
  private String fileId;

  // 게시판 아이디
  private String boardId;

  // 원본파일명
  private String orgFileName;

  // 서버파일명
  private String serverFileName;

  // 확장자
  private String extension;

  // 파일 크기
  private String fileSize;

  // 서버경로
  private String serverFilePath;

  // 웹경로
  private String webFilePath;

  // 등록일
  private String regDt;
}
