package com.example.cucucook.service.impl;

import com.example.cucucook.service.SmsService;
import org.springframework.stereotype.Service;

@Service
public class SmsServiceImpl implements SmsService {

    @Override
    public void send(String phoneNumber, String message) {
        // SMS 전송 로직 구현
        // 예: SMS API를 호출하여 SMS 발송
    }
}
