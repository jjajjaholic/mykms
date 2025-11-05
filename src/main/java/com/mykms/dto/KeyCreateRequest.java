package com.mykms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 키 생성 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "키 생성 요청")
public class KeyCreateRequest {

    @NotBlank(message = "키 이름은 필수입니다")
    @Size(min = 1, max = 255, message = "키 이름은 1~255자 사이여야 합니다")
    @Schema(description = "키 이름", example = "my-encryption-key")
    private String keyName;

    @Min(value = 128, message = "키 크기는 최소 128비트여야 합니다")
    @Max(value = 512, message = "키 크기는 최대 512비트여야 합니다")
    @Schema(description = "키 크기 (비트)", example = "256", defaultValue = "256")
    private Integer keySize = 256;

    @Size(max = 1000, message = "설명은 최대 1000자까지 가능합니다")
    @Schema(description = "키 설명", example = "데이터 암호화를 위한 대칭키")
    private String description;
}
