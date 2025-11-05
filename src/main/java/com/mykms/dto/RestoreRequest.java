package com.mykms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 복원 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestoreRequest {

    @NotBlank(message = "백업 파일명은 필수입니다")
    @Schema(description = "복원할 백업 파일명", example = "backup_20250105_120000.db")
    private String filename;
}
