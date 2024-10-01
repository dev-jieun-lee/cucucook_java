package com.example.cucucook.config;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

public class JwtTokenFilter extends UsernamePasswordAuthenticationFilter {

  private final JwtTokenProvider tokenProvider;

  public JwtTokenFilter(JwtTokenProvider tokenProvider) {
    this.tokenProvider = tokenProvider;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    String token = getTokenFromCookies(httpRequest);

    if (token != null && tokenProvider.validateToken(token)) {
      String userId = tokenProvider.getUserId(token);
      UserDetails userDetails = tokenProvider.loadUserByUserId(userId);
      if (userDetails != null) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, null,
            userDetails.getAuthorities()));
      } else {
        System.out.println("유효하지 않은 사용자 정보입니다. member_id: " + userId);
      }
    }
    chain.doFilter(request, response);
  }

  private String getTokenFromCookies(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies(); // 수정된 부분
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if ("auth_token".equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
    }
    return null;
  }
}