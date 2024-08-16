package com.example.cucucook.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberRecipeImages {

    //이미지 아이디
    private String imgId;

    //원본이미지명
    private String orgImgName;

    //서버이미지명
    private String serverImgName;

    //확장자
    private String extension;

    //이미지 파일크기
    private String imgFileSize;

    //서버이미지경로
    private String serverImgPath;

    //웹이미지경로
    private String webImgPath;

}
