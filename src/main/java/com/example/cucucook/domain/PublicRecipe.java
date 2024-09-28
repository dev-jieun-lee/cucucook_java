package com.example.cucucook.domain;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PublicRecipe implements Serializable {

  @SerializedName("RCP_SEQ")
  private String rcpSeq; // 일련번호

  @SerializedName("RCP_NM")
  private String rcpNm; // 메뉴명

  @SerializedName("RCP_WAY2")
  private String rcpWay2; // 조리방법

  @SerializedName("RCP_PAT2")
  private String rcpPat2; // 요리종류

  @SerializedName("INFO_WGT")
  private String infoWgt; // 중량(1인분)

  @SerializedName("INFO_ENG")
  private String infoEng; // 열량

  @SerializedName("INFO_CAR")
  private String infoCar; // 탄수화물

  @SerializedName("INFO_PRO")
  private String infoPro; // 단백질

  @SerializedName("INFO_FAT")
  private String infoFat; // 지방

  @SerializedName("INFO_NA")
  private String infoNa; // 나트륨

  @SerializedName("HASH_TAG")
  private String hashTag; // 해쉬태그

  @SerializedName("ATT_FILE_NO_MAIN")
  private String attFileNoMain; // 이미지경로(소)

  @SerializedName("ATT_FILE_NO_MK")
  private String attFileNoMk; // 이미지경로(대)

  @SerializedName("RCP_PARTS_DTLS")
  private String rcpPartsDtls; // 재료정보

  @SerializedName("MANUAL01")
  private String manual01; // 만드는법_01
  @SerializedName("MANUAL_IMG01")
  private String manualImg01; // 만드는법_이미지_01

  @SerializedName("MANUAL02")
  private String manual02; // 만드는법_02
  @SerializedName("MANUAL_IMG02")
  private String manualImg02; // 만드는법_이미지_02

  @SerializedName("MANUAL03")
  private String manual03; // 만드는법_03
  @SerializedName("MANUAL_IMG03")
  private String manualImg03; // 만드는법_이미지_03

  @SerializedName("MANUAL04")
  private String manual04; // 만드는법_04
  @SerializedName("MANUAL_IMG04")
  private String manualImg04; // 만드는법_이미지_04

  @SerializedName("MANUAL05")
  private String manual05; // 만드는법_05
  @SerializedName("MANUAL_IMG05")
  private String manualImg05; // 만드는법_이미지_05

  @SerializedName("MANUAL06")
  private String manual06; // 만드는법_06
  @SerializedName("MANUAL_IMG06")
  private String manualImg06; // 만드는법_이미지_06

  @SerializedName("MANUAL07")
  private String manual07; // 만드는법_07
  @SerializedName("MANUAL_IMG07")
  private String manualImg07; // 만드는법_이미지_07

  @SerializedName("MANUAL08")
  private String manual08; // 만드는법_08
  @SerializedName("MANUAL_IMG08")
  private String manualImg08; // 만드는법_이미지_08

  @SerializedName("MANUAL09")
  private String manual09; // 만드는법_09
  @SerializedName("MANUAL_IMG09")
  private String manualImg09; // 만드는법_이미지_09

  @SerializedName("MANUAL10")
  private String manual10; // 만드는법_10
  @SerializedName("MANUAL_IMG10")
  private String manualImg10; // 만드는법_이미지_10

  @SerializedName("MANUAL11")
  private String manual11; // 만드는법_11
  @SerializedName("MANUAL_IMG11")
  private String manualImg11; // 만드는법_이미지_11

  @SerializedName("MANUAL12")
  private String manual12; // 만드는법_12
  @SerializedName("MANUAL_IMG12")
  private String manualImg12; // 만드는법_이미지_12

  @SerializedName("MANUAL13")
  private String manual13; // 만드는법_13
  @SerializedName("MANUAL_IMG13")
  private String manualImg13; // 만드는법_이미지_13

  @SerializedName("MANUAL14")
  private String manual14; // 만드는법_14
  @SerializedName("MANUAL_IMG14")
  private String manualImg14; // 만드는법_이미지_14

  @SerializedName("MANUAL15")
  private String manual15; // 만드는법_15
  @SerializedName("MANUAL_IMG15")
  private String manualImg15; // 만드는법_이미지_15

  @SerializedName("MANUAL16")
  private String manual16; // 만드는법_16
  @SerializedName("MANUAL_IMG16")
  private String manualImg16; // 만드는법_이미지_16

  @SerializedName("MANUAL17")
  private String manual17; // 만드는법_17
  @SerializedName("MANUAL_IMG17")
  private String manualImg17; // 만드는법_이미지_17

  @SerializedName("MANUAL18")
  private String manual18; // 만드는법_18
  @SerializedName("MANUAL_IMG18")
  private String manualImg18; // 만드는법_이미지_18

  @SerializedName("MANUAL19")
  private String manual19; // 만드는법_19
  @SerializedName("MANUAL_IMG19")
  private String manualImg19; // 만드는법_이미지_19

  @SerializedName("MANUAL20")
  private String manual20; // 만드는법_20
  @SerializedName("MANUAL_IMG20")
  private String manualImg20; // 만드는법_이미지_20

  @SerializedName("RCP_NA_TIP")
  private String rcpNaTip; // 저감 조리법 TIP

}
