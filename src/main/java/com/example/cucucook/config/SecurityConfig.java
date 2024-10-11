package com.example.cucucook.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Value("${server.serverAddress}")
  private String serverAddress;

  // bean으로 등록되지않은 객체 생성
  private final JwtTokenProvider tokenProvider;

  public SecurityConfig(JwtTokenProvider tokenProvider) {
    this.tokenProvider = tokenProvider;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

    JwtTokenFilter jwtTokenFilter = new JwtTokenFilter(tokenProvider);
    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정
        .csrf(csrf -> csrf.disable()) // CSRF 비활성화
        .authorizeHttpRequests(authorize -> authorize
            // 로그인된 사용자만
            .requestMatchers("/api/recipe/insertMemberRecipe",
                "/api/recipe/updateMemberRecipe",
                "/api/recipe/deleteMemberRecipe",
                "/api/recipe/insertRecipeComment",
                "/api/recipe/updateRecipeComment",
                "/api/recipe/deleteRecipeComment",
                "/api/recipe/deleteRecipeCommentHasChild",
                "/api/recipe/insertMemberRecipeLike",
                "/api/recipe/deleteMemberRecipeLike",
                "/api/recipe/insertRecipeCategory",
                "/api/recipe/updateRecipeCategory",
                "/api/recipe/deleteRecipeCategory")
            .authenticated()
            // 특정 권한만
            .requestMatchers("/api/admin/**").hasAnyAuthority("ADMIN", "SUPER_ADMIN")
            .anyRequest().permitAll() // 모든 요청 허용
        )
        .exceptionHandling(exceptionHandling -> exceptionHandling
            .authenticationEntryPoint((request, response, authException) -> {
              // 인증 실패 (401) - 토큰이 없거나 잘못된 경우
              response.setContentType("application/json;charset=UTF-8");
              response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
              response.getWriter().write("{ \"message\": \"E_AUTH\", \"status\": 401 }");
            })
            .accessDeniedHandler((request, response, accessDeniedException) -> {
              // 권한 부족 (403) - 사용자가 해당 자원에 접근 권한이 없을 때
              response.setContentType("application/json;charset=UTF-8");
              response.setStatus(HttpServletResponse.SC_FORBIDDEN);
              response.getWriter().write("{ \"message\": \"E_ROLE\", \"status\": 403 }");
            }))
        .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
        .formLogin(formLogin -> formLogin
            .loginPage("/login") // 사용자 정의 로그인 페이지 URL
            .permitAll() // 로그인 페이지는 모든 사용자에게 허용
        ).logout(logout -> logout
            .logoutUrl("/logout") // 로그아웃 URL 설정
            .logoutSuccessUrl("/") // 로그아웃 성공 시 리다이렉트 URL
            .invalidateHttpSession(true) // 세션 무효화
            .deleteCookies("JSESSIONID") // 쿠키 삭제
        );
    return http.build();
  }

  // CORS 설정을 위한 메서드 추가
  private CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.addAllowedOrigin(serverAddress);
    configuration.addAllowedMethod("*");
    configuration.addAllowedHeader("*");
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
