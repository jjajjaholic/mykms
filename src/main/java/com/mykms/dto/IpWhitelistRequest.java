package com.mykms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * IP 화이트리스트 생성/수정 요청
 */
@Data
@Schema(description = "IP 화이트리스트 생성/수정 요청")
public class IpWhitelistRequest {

    @NotBlank(message = "IP 주소는 필수입니다")
    @Pattern(
            regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(/(3[0-2]|[12]?[0-9]))?$",
            message = "유효한 IP 주소 또는 CIDR 형식이어야 합니다 (예: 192.168.1.100 또는 192.168.1.0/24)"
    )
    @Schema(description = "IP 주소 (CIDR 표기법 지원)", example = "192.168.1.100")
    private String ipAddress;

    @Schema(description = "설명", example = "개발 서버")
    private String description;

    @Schema(description = "활성화 여부", example = "true")
    private Boolean enabled = true;
}
