package com.mykms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 키 업데이트 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "키 업데이트 요청")
public class KeyUpdateRequest {

    @Size(min = 1, max = 255, message = "키 이름은 1~255자 사이여야 합니다")
    @Schema(description = "새로운 키 이름", example = "updated-key-name")
    private String keyName;

    @Size(max = 1000, message = "설명은 최대 1000자까지 가능합니다")
    @Schema(description = "새로운 키 설명", example = "업데이트된 설명")
    private String description;

    @Schema(description = "키 로테이션 (새로운 키 값 생성)", example = "false", defaultValue = "false")
    private Boolean rotate = false;
}
