package com.mykms.controller;

import com.mykms.dto.*;
import com.mykms.service.KeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 키 관리 REST Controller
 */
@RestController
@RequestMapping("/api/keys")
@RequiredArgsConstructor
@Tag(name = "Keys", description = "키 관리 API")
@SecurityRequirement(name = "basicAuth")
public class KeyController {

    private final KeyService keyService;

    @PostMapping
    @Operation(summary = "키 생성", description = "새로운 대칭키를 생성합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "키 생성 성공",
                    content = @Content(schema = @Schema(implementation = KeyResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 키 이름",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<KeyResponse> createKey(@Valid @RequestBody KeyCreateRequest request) {
        KeyResponse response = keyService.createKey(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "키 목록 조회", description = "저장된 모든 키의 목록을 조회합니다 (키 값 제외)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "키 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<KeyListResponse>> listKeys() {
        List<KeyListResponse> response = keyService.listKeys();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{keyId}")
    @Operation(summary = "키 조회", description = "특정 키의 정보를 조회합니다 (키 값 포함)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "키 조회 성공",
                    content = @Content(schema = @Schema(implementation = KeyResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "키를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<KeyResponse> getKey(@PathVariable String keyId) {
        KeyResponse response = keyService.getKey(keyId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/name/{keyName}")
    @Operation(summary = "이름으로 키 조회", description = "키 이름으로 키를 조회합니다 (키 값 포함)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "키 조회 성공",
                    content = @Content(schema = @Schema(implementation = KeyResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "키를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<KeyResponse> getKeyByName(@PathVariable String keyName) {
        KeyResponse response = keyService.getKeyByName(keyName);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{keyId}")
    @Operation(summary = "키 업데이트", description = "키의 정보를 업데이트하거나 키를 로테이션합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "키 업데이트 성공",
                    content = @Content(schema = @Schema(implementation = KeyResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "키를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<KeyResponse> updateKey(
            @PathVariable String keyId,
            @Valid @RequestBody KeyUpdateRequest request) {
        KeyResponse response = keyService.updateKey(keyId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{keyId}")
    @Operation(summary = "키 삭제", description = "특정 키를 삭제합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "키 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "키를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteKey(@PathVariable String keyId) {
        keyService.deleteKey(keyId);
        return ResponseEntity.noContent().build();
    }
}
