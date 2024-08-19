package com.example.cucucook;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.cucucook.mapper") // 매퍼 인터페이스가 위치한 패키지 지정
public class CucucookApplication {

    public static void main(String[] args) {
        SpringApplication.run(CucucookApplication.class, args);
    }

}
