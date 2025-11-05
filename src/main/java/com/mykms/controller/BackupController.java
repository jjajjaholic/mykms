package com.mykms.controller;

import com.mykms.dto.BackupListResponse;
import com.mykms.dto.BackupResponse;
import com.mykms.dto.RestoreRequest;
import com.mykms.service.BackupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * 백업 및 복원 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/backup")
@RequiredArgsConstructor
@Tag(name = "Backup", description = "데이터베이스 백업 및 복원 API")
@SecurityRequirement(name = "basicAuth")
public class BackupController {

    private final BackupService backupService;

    /**
     * 데이터베이스 백업 생성
     */
    @PostMapping
    @Operation(summary = "백업 생성", description = "현재 데이터베이스를 백업 파일로 생성합니다")
    public ResponseEntity<BackupResponse> createBackup() {
        try {
            log.info("백업 생성 요청");
            BackupResponse response = backupService.createBackup();
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("백업 생성 실패", e);
            return ResponseEntity.internalServerError()
                    .body(BackupResponse.builder()
                            .message("백업 생성 실패: " + e.getMessage())
                            .build());
        }
    }

    /**
     * 백업 파일 목록 조회
     */
    @GetMapping("/list")
    @Operation(summary = "백업 목록 조회", description = "생성된 백업 파일 목록을 조회합니다")
    public ResponseEntity<BackupListResponse> listBackups() {
        try {
            log.info("백업 목록 조회 요청");
            BackupListResponse response = backupService.listBackups();
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("백업 목록 조회 실패", e);
            return ResponseEntity.internalServerError()
                    .body(BackupListResponse.builder()
                            .backups(java.util.Collections.emptyList())
                            .total(0)
                            .build());
        }
    }

    /**
     * 백업 복원
     */
    @PostMapping("/restore")
    @Operation(summary = "백업 복원", description = "백업 파일에서 데이터베이스를 복원합니다")
    public ResponseEntity<BackupResponse> restoreBackup(@Valid @RequestBody RestoreRequest request) {
        try {
            log.info("백업 복원 요청: {}", request.getFilename());
            BackupResponse response = backupService.restoreFromBackup(request.getFilename());
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("백업 복원 실패", e);
            return ResponseEntity.internalServerError()
                    .body(BackupResponse.builder()
                            .message("백업 복원 실패: " + e.getMessage())
                            .build());
        }
    }

    /**
     * 백업 파일 다운로드
     */
    @GetMapping("/download/{filename}")
    @Operation(summary = "백업 다운로드", description = "백업 파일을 다운로드합니다")
    public ResponseEntity<Resource> downloadBackup(@PathVariable String filename) {
        try {
            log.info("백업 다운로드 요청: {}", filename);
            Resource resource = backupService.loadBackupAsResource(filename);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (IOException e) {
            log.error("백업 다운로드 실패", e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 백업 파일 삭제
     */
    @DeleteMapping("/{filename}")
    @Operation(summary = "백업 삭제", description = "백업 파일을 삭제합니다")
    public ResponseEntity<BackupResponse> deleteBackup(@PathVariable String filename) {
        try {
            log.info("백업 삭제 요청: {}", filename);
            // TODO: 백업 삭제 기능 구현
            return ResponseEntity.ok(BackupResponse.builder()
                    .filename(filename)
                    .message("백업 삭제 기능은 추후 구현될 예정입니다")
                    .build());
        } catch (Exception e) {
            log.error("백업 삭제 실패", e);
            return ResponseEntity.internalServerError()
                    .body(BackupResponse.builder()
                            .message("백업 삭제 실패: " + e.getMessage())
                            .build());
        }
    }
}
