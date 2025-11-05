package com.mykms.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 키 목록 응답 DTO (키 값 제외)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "키 목록 응답")
public class KeyListResponse {

    @Schema(description = "키 ID (UUID)", example = "123e4567-e89b-12d3-a456-426614174000")
    private String keyId;

    @Schema(description = "키 이름", example = "my-encryption-key")
    private String keyName;

    @Schema(description = "키 크기 (비트)", example = "256")
    private Integer keySize;

    @Schema(description = "키 설명", example = "데이터 암호화를 위한 대칭키")
    private String description;

    @Schema(description = "생성 시간", example = "2025-01-01T00:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "수정 시간", example = "2025-01-01T00:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
