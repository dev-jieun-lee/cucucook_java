package com.example.cucucook.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
//API 성공여부 반환
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    public boolean isSuccess() {
        return success;
    }
}
