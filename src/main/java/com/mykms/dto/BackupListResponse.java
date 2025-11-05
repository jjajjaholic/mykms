package com.mykms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 백업 목록 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackupListResponse {

    private List<BackupFileInfo> backups;
    private Integer total;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BackupFileInfo {
        private String filename;
        private Long size;
        private LocalDateTime createdAt;
    }
}
