package com.example.demo.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// [연습②] AdminApiController 의 오류 처리를 한 곳에 모은다
@RestControllerAdvice(assignableTypes = AdminApiController.class) // 화면 컨트롤러에는 영향 없음
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class) // 서비스가 던진 검사 실패
    public ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest() // 400
                .body(Map.of("error", e.getMessage())); // {"error":"..."}
    }
}