package com.mykms.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mykms.entity.KeyEntity;
import com.mykms.repository.KeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 시스템 종료 시 DB 정보를 파일로 저장하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShutdownService {

    private final KeyRepository keyRepository;
    private static final String BACKUP_DIR = "shutdown-backups";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /**
     * 시스템 종료 시 DB 정보를 파일로 저장
     */
    public void saveDbInfoOnShutdown() {
        try {
            log.info("=".repeat(70));
            log.info("시스템 종료 감지 - DB 정보 저장 시작");
            log.info("=".repeat(70));

            // 백업 디렉토리 생성
            Path backupPath = Paths.get(BACKUP_DIR);
            if (!Files.exists(backupPath)) {
                Files.createDirectories(backupPath);
                log.info("백업 디렉토리 생성: {}", backupPath.toAbsolutePath());
            }

            // 모든 키 정보 조회
            List<KeyEntity> allKeys = keyRepository.findAll();
            log.info("조회된 키 개수: {}", allKeys.size());

            // 저장할 정보 구성
            Map<String, Object> shutdownInfo = new HashMap<>();
            shutdownInfo.put("shutdownTime", LocalDateTime.now());
            shutdownInfo.put("totalKeys", allKeys.size());
            shutdownInfo.put("keys", allKeys);

            // 시스템 정보 추가
            Map<String, Object> systemInfo = new HashMap<>();
            systemInfo.put("javaVersion", System.getProperty("java.version"));
            systemInfo.put("osName", System.getProperty("os.name"));
            systemInfo.put("osVersion", System.getProperty("os.version"));
            systemInfo.put("userDir", System.getProperty("user.dir"));
            systemInfo.put("totalMemory", Runtime.getRuntime().totalMemory());
            systemInfo.put("freeMemory", Runtime.getRuntime().freeMemory());
            shutdownInfo.put("systemInfo", systemInfo);

            // JSON 파일로 저장
            String timestamp = LocalDateTime.now().format(FORMATTER);
            String fileName = String.format("db-shutdown-%s.json", timestamp);
            File outputFile = backupPath.resolve(fileName).toFile();

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            objectMapper.writeValue(outputFile, shutdownInfo);

            log.info("DB 정보 저장 완료: {}", outputFile.getAbsolutePath());
            log.info("파일 크기: {} bytes", outputFile.length());
            log.info("=".repeat(70));

        } catch (IOException e) {
            log.error("DB 정보 저장 중 오류 발생", e);
        } catch (Exception e) {
            log.error("예상치 못한 오류 발생", e);
        }
    }

    /**
     * 긴급 종료 시 간단한 정보만 저장
     */
    public void saveEmergencyInfo(String reason, Throwable throwable) {
        try {
            log.error("=".repeat(70));
            log.error("긴급 종료 감지 - 오류 정보 저장 시작");
            log.error("종료 사유: {}", reason);
            log.error("=".repeat(70));

            // 백업 디렉토리 생성
            Path backupPath = Paths.get(BACKUP_DIR);
            if (!Files.exists(backupPath)) {
                Files.createDirectories(backupPath);
            }

            // 긴급 정보 구성
            Map<String, Object> emergencyInfo = new HashMap<>();
            emergencyInfo.put("shutdownTime", LocalDateTime.now());
            emergencyInfo.put("shutdownReason", reason);

            if (throwable != null) {
                Map<String, Object> errorInfo = new HashMap<>();
                errorInfo.put("message", throwable.getMessage());
                errorInfo.put("type", throwable.getClass().getName());

                StackTraceElement[] stackTrace = throwable.getStackTrace();
                if (stackTrace != null && stackTrace.length > 0) {
                    errorInfo.put("location", stackTrace[0].toString());
                }
                emergencyInfo.put("error", errorInfo);
            }

            // 키 개수만 조회 (빠른 처리)
            try {
                long keyCount = keyRepository.count();
                emergencyInfo.put("totalKeys", keyCount);
                log.info("현재 저장된 키 개수: {}", keyCount);
            } catch (Exception e) {
                log.warn("키 개수 조회 실패: {}", e.getMessage());
                emergencyInfo.put("totalKeys", "조회 실패");
            }

            // JSON 파일로 저장
            String timestamp = LocalDateTime.now().format(FORMATTER);
            String fileName = String.format("emergency-shutdown-%s.json", timestamp);
            File outputFile = backupPath.resolve(fileName).toFile();

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            objectMapper.writeValue(outputFile, emergencyInfo);

            log.error("긴급 종료 정보 저장 완료: {}", outputFile.getAbsolutePath());
            log.error("=".repeat(70));

        } catch (Exception e) {
            log.error("긴급 종료 정보 저장 실패", e);
        }
    }
}
