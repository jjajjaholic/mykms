package com.mykms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * IP 화이트리스트 목록 응답
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "IP 화이트리스트 목록 응답")
public class IpWhitelistListResponse {

    @Schema(description = "IP 화이트리스트 목록")
    private List<IpWhitelistResponse> whitelists;

    @Schema(description = "전체 개수", example = "10")
    private int total;
}
