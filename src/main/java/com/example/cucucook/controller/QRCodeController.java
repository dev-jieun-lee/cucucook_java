package com.example.cucucook.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
public class QRCodeController {

    @GetMapping("/generate-qr")
    public void generateQRCode(@RequestParam String message, HttpServletResponse response) throws IOException, WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, String> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        // QR 코드 생성
        BitMatrix bitMatrix = qrCodeWriter.encode(message, BarcodeFormat.QR_CODE, 300, 300, hints);

        // 응답으로 QR 코드 이미지 전송
        response.setContentType("image/png");
        ImageIO.write(MatrixToImageWriter.toBufferedImage(bitMatrix), "PNG", response.getOutputStream());
    }
}
