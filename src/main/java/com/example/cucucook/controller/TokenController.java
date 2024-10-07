package com.example.cucucook.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.cucucook.service.TokenService;

@RestController
@RequestMapping("/api/tokens")
public class TokenController {

  @Autowired
  private TokenService tokenService;

  @GetMapping("/verify")
  public boolean verifyToken(@RequestParam String token, @RequestParam String tokenType) {
    return tokenService.verifyToken(token, tokenType);
  }

  @DeleteMapping
  public void deleteToken(@RequestParam String token) {
    tokenService.deleteToken(token);
  }
}
