package com.mykms.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 루트 및 헬스 체크 컨트롤러
 */
@RestController
@Tag(name = "Root", description = "루트 및 헬스 체크 API")
public class RootController {

    @GetMapping("/")
    @Operation(summary = "루트 엔드포인트", description = "API 정보를 반환합니다")
    public Map<String, String> root() {
        return Map.of(
                "message", "MyKMS - Key Management System",
                "version", "1.0.0",
                "docs", "/swagger-ui/index.html",
                "api-docs", "/v3/api-docs"
        );
    }

    @GetMapping("/health")
    @Operation(summary = "헬스 체크", description = "서비스 상태를 확인합니다")
    public Map<String, String> health() {
        return Map.of(
                "status", "healthy",
                "service", "MyKMS"
        );
    }
}
