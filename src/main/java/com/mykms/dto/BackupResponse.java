package com.mykms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 백업 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackupResponse {

    private String filename;
    private String filepath;
    private Long size;
    private LocalDateTime createdAt;
    private String message;
}
