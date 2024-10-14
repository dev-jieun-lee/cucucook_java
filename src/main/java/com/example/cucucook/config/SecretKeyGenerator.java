package com.example.cucucook.config;

import java.security.SecureRandom;
import java.util.Base64;

public class SecretKeyGenerator {
  public static void main(String[] args) {
    SecureRandom random = new SecureRandom();
    byte[] key = new byte[32]; // 256-bit key
    random.nextBytes(key);
    String base64Key = Base64.getEncoder().encodeToString(key);
  }
}
