package com.example.cucucook.common;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * 파일 업로드를 처리하는 유틸리티 클래스.
 */
@Component
public class FileUploadUtil {

  // 파일 저장 경로
  @Value("${file.dir}")
  private String fileDir;

  // 파일 웹경로
  @Value("${file.web.dir}")
  private String fileWebDir;

  /**
   * 폴더 이름을 받아 저장 경로를 반환.
   *
   * @param folderNm 폴더 이름
   * @return 저장 경로
   */
  public String getFileDir(String folderNm) {
    String finalFileDir = fileDir;
    if (folderNm != null && !folderNm.isEmpty()) {
      finalFileDir += folderNm;
    }
    return finalFileDir;
  }

  /**
   * 웹경로
   *
   * @param folderNm 폴더 이름
   * @return 저장 경로
   */
  public String getFileWebDir(String folderNm) {
    String finalFileDir = fileWebDir;
    if (folderNm != null && !folderNm.isEmpty()) {
      finalFileDir += folderNm;
    }
    return finalFileDir;
  }

  /**
   * 확장자 제외 원본 파일명 가져오기
   *
   * @param originalFileName 원본 파일 이름
   * @param fileType         파일 타입 (MIME 타입)
   * @return 원본 파일명
   */
  public String extractOriginalFileName(String originalFileName) {

    if (originalFileName != null && originalFileName.contains(".")) {
      // 마지막 '.' 이전까지의 문자열을 추출
      return originalFileName.substring(0, originalFileName.lastIndexOf('.'));
    }
    return originalFileName;
  }

  /**
   * 파일 확장자를 추출하여 반환.
   *
   * @param originalFileName 원본 파일 이름
   * @param fileType         파일 타입 (MIME 타입)
   * @return 확장자
   * @throws IllegalArgumentException 지원하지 않는 확장자일 경우 예외 발생
   */
  public String extractExtension(String originalFileName, String fileType) {
    int fileExtensionIndex = originalFileName.lastIndexOf('.');
    if (fileExtensionIndex == -1) {
      throw new IllegalArgumentException("파일 이름에 확장자가 없습니다.");
    }

    String fileExtension = originalFileName.substring(fileExtensionIndex + 1);
    if (validateExtension(fileExtension, fileType)) {
      return fileExtension;
    }

    throw new IllegalArgumentException("E_SUPPORTED_FILE_EXT");
  }

  /**
   * 주어진 파일 확장자가 해당 파일 타입에 허용되는지 검증.
   *
   * @param fileExtension 파일 확장자
   * @param fileType      파일 타입 (MIME 타입)
   * @return 유효한 확장자인 경우 true, 그렇지 않으면 false
   */
  public boolean validateExtension(String fileExtension, String fileType) {
    String[] allowedExtensions = getAllowedExtensions(fileType);
    if (allowedExtensions == null) {
      throw new IllegalArgumentException("E_SUPPORTED_FILE_EXT");
    }

    // 대소문자 구분 없이 비교
    String normalizedFileExtension = fileExtension.toLowerCase();
    return Arrays.stream(allowedExtensions).anyMatch(value -> value.equals(normalizedFileExtension));
  }

  /**
   * 파일 타입에 따른 허용 확장자를 반환.
   *
   * @param fileType 파일 타입 (MIME 타입)
   * @return 허용된 확장자 배열
   * @throws IllegalArgumentException 지원하지 않는 파일 형식일 경우 예외 발생
   */
  private String[] getAllowedExtensions(String fileType) {
    return switch (fileType) {
      case "image/jpeg" -> new String[] { "jpg", "jpeg" };
      case "image/png" -> new String[] { "png" };
      case "image/gif" -> new String[] { "gif" };
      case "image/bmp" -> new String[] { "bmp" };
      case "image/svg+xml" -> new String[] { "svg" };
      case "application/pdf" -> new String[] { "pdf" };
      case "application/msword" -> new String[] { "doc", "docx" };
      case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> new String[] { "docx" };
      case "application/vnd.ms-excel" -> new String[] { "xls", "xlsx" };
      case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> new String[] { "xlsx" };
      case "application/vnd.ms-powerpoint" -> new String[] { "ppt", "pptx" };
      case "application/vnd.openxmlformats-officedocument.presentationml.presentation" -> new String[] { "pptx" };
      case "text/plain" -> new String[] { "txt" };
      case "application/vnd.hancom.hwp" -> new String[] { "hwp" };
      case "application/vnd.hancom.hwpx" -> new String[] { "hwpx" };
      case "application/vnd.hancom.hwt" -> new String[] { "hwt" };
      case "application/zip" -> new String[] { "zip" };
      case "application/x-7z-compressed" -> new String[] { "7z" };
      case "audio/mpeg" -> new String[] { "mp3" };
      case "audio/wav" -> new String[] { "wav" };
      case "video/mp4" -> new String[] { "mp4" };
      case "video/quicktime" -> new String[] { "mov" };
      case "video/x-msvideo" -> new String[] { "avi" };
      case "video/x-matroska" -> new String[] { "mkv" };
      default -> throw new IllegalArgumentException("파일형식오류 " + fileType);
    };
  }

  /**
   * 파일 저장에 사용할 고유한 파일명을 생성.
   *
   * @param originalFileName 원본 파일 이름
   * @return UUID 기반의 고유한 파일명
   */
  public String organizeStoredFileName(String originalFileName) {
    return UUID.randomUUID().toString();
  }

  /**
   * 파일을 지정된 폴더에 업로드.
   *
   * @param file     업로드할 파일
   * @param folderNm 저장할 폴더명
   * @return 저장된 파일의 이름 (확장자 포함)
   * @throws IOException 파일 저장 중 오류 발생
   */
  public String uploadFile(MultipartFile file, String folderNm) throws IOException {
    // 원래 파일 이름
    String fileName = extractOriginalFileName(file.getOriginalFilename());
    if (fileName == null) {
      throw new IllegalArgumentException("E_IS_FILE_NAME");
    }

    // 저장될 파일 이름
    String serverFileName = organizeStoredFileName(fileName);

    // 확장자 추출
    String ext = extractExtension(file.getOriginalFilename(), file.getContentType());

    // 동적으로 생성된 저장 경로
    String saveDir = getFileDir(folderNm); // folderNm에 따라 경로 결정
    File directory = new File(saveDir);

    // 디렉토리 생성
    if (!directory.exists()) {
      directory.mkdirs();
    }

    // 파일 저장
    File dest = new File(directory, serverFileName + "." + ext);

    try {
      // 실제 파일 저장
      file.transferTo(dest);

    } catch (IOException e) {
      // 파일 입출력 관련 오류 처리
      System.err.println("파일 저장 중 오류 발생: " + e.getMessage());
      throw new IllegalArgumentException("E_IS_FILE_NAME");
    } catch (IllegalStateException e) {
      System.err.println(e.getMessage());
      throw new IllegalArgumentException("E_IS_FILE_NAME");
    }
    return serverFileName;
  }

  /**
   * 파일 삭제(해당 폴더가 비어있으면 최종적으로 삭제)
   *
   * @param
   * @return 삭제 실패 이유
   * @throws IOException 파일 삭제 중 오류 발생
   */
  public void deleteFile(String filePath, String folderPath) throws IOException {

    File file = new File(filePath);

    if (file.exists()) {
      // 파일 삭제 시도
      if (!file.delete()) {
        // 파일 삭제에 실패한 경우 IOException 던지기
        throw new IOException("E_DEL_FILE");
      }
    }

    File directory = new File(folderPath);

    if (directory.exists() && directory.isDirectory()) {
      // 폴더가 존재하고 디렉토리인 경우
      File[] files = directory.listFiles();

      if (files == null || files.length == 0) {
        if (!directory.delete()) {
          // 파일 삭제에 실패한 경우 IOException 던지기
          throw new IOException("E_DEL_FILE");
        }
      }
    }
  }

}
