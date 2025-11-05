package com.mykms.dto;

import com.mykms.entity.IpWhitelistEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * IP 화이트리스트 응답
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "IP 화이트리스트 응답")
public class IpWhitelistResponse {

    @Schema(description = "ID", example = "1")
    private Long id;

    @Schema(description = "IP 주소", example = "192.168.1.100")
    private String ipAddress;

    @Schema(description = "설명", example = "개발 서버")
    private String description;

    @Schema(description = "활성화 여부", example = "true")
    private Boolean enabled;

    @Schema(description = "생성 시간")
    private LocalDateTime createdAt;

    @Schema(description = "수정 시간")
    private LocalDateTime updatedAt;

    /**
     * Entity를 Response로 변환
     */
    public static IpWhitelistResponse from(IpWhitelistEntity entity) {
        return IpWhitelistResponse.builder()
                .id(entity.getId())
                .ipAddress(entity.getIpAddress())
                .description(entity.getDescription())
                .enabled(entity.getEnabled())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
