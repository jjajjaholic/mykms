package com.mykms.controller;

import com.mykms.dto.IpWhitelistListResponse;
import com.mykms.dto.IpWhitelistRequest;
import com.mykms.dto.IpWhitelistResponse;
import com.mykms.entity.IpWhitelistEntity;
import com.mykms.repository.IpWhitelistRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * IP 화이트리스트 관리 컨트롤러
 */
@RestController
@RequestMapping("/api/ip-whitelist")
@RequiredArgsConstructor
@Tag(name = "IP Whitelist", description = "IP 화이트리스트 관리 API")
public class IpWhitelistController {

    private final IpWhitelistRepository ipWhitelistRepository;

    /**
     * IP 화이트리스트 생성
     */
    @PostMapping
    @Operation(summary = "IP 화이트리스트 생성", description = "새로운 IP 주소를 화이트리스트에 추가합니다")
    public ResponseEntity<IpWhitelistResponse> createWhitelist(@Valid @RequestBody IpWhitelistRequest request) {
        // 중복 체크
        if (ipWhitelistRepository.existsByIpAddress(request.getIpAddress())) {
            throw new IllegalArgumentException("이미 등록된 IP 주소입니다: " + request.getIpAddress());
        }

        IpWhitelistEntity entity = IpWhitelistEntity.builder()
                .ipAddress(request.getIpAddress())
                .description(request.getDescription())
                .enabled(request.getEnabled() != null ? request.getEnabled() : true)
                .build();

        IpWhitelistEntity saved = ipWhitelistRepository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(IpWhitelistResponse.from(saved));
    }

    /**
     * IP 화이트리스트 목록 조회
     */
    @GetMapping
    @Operation(summary = "IP 화이트리스트 목록 조회", description = "모든 IP 화이트리스트를 조회합니다")
    public ResponseEntity<IpWhitelistListResponse> getWhitelists() {
        List<IpWhitelistEntity> entities = ipWhitelistRepository.findAll();
        List<IpWhitelistResponse> responses = entities.stream()
                .map(IpWhitelistResponse::from)
                .collect(Collectors.toList());

        IpWhitelistListResponse listResponse = IpWhitelistListResponse.builder()
                .whitelists(responses)
                .total(responses.size())
                .build();

        return ResponseEntity.ok(listResponse);
    }

    /**
     * IP 화이트리스트 조회
     */
    @GetMapping("/{id}")
    @Operation(summary = "IP 화이트리스트 조회", description = "ID로 IP 화이트리스트를 조회합니다")
    public ResponseEntity<IpWhitelistResponse> getWhitelist(@PathVariable Long id) {
        IpWhitelistEntity entity = ipWhitelistRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("IP 화이트리스트를 찾을 수 없습니다: " + id));

        return ResponseEntity.ok(IpWhitelistResponse.from(entity));
    }

    /**
     * IP 화이트리스트 수정
     */
    @PutMapping("/{id}")
    @Operation(summary = "IP 화이트리스트 수정", description = "IP 화이트리스트를 수정합니다")
    public ResponseEntity<IpWhitelistResponse> updateWhitelist(
            @PathVariable Long id,
            @Valid @RequestBody IpWhitelistRequest request) {

        IpWhitelistEntity entity = ipWhitelistRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("IP 화이트리스트를 찾을 수 없습니다: " + id));

        // IP 주소가 변경되는 경우 중복 체크
        if (!entity.getIpAddress().equals(request.getIpAddress()) &&
                ipWhitelistRepository.existsByIpAddress(request.getIpAddress())) {
            throw new IllegalArgumentException("이미 등록된 IP 주소입니다: " + request.getIpAddress());
        }

        entity.setIpAddress(request.getIpAddress());
        entity.setDescription(request.getDescription());
        entity.setEnabled(request.getEnabled() != null ? request.getEnabled() : true);

        IpWhitelistEntity updated = ipWhitelistRepository.save(entity);
        return ResponseEntity.ok(IpWhitelistResponse.from(updated));
    }

    /**
     * IP 화이트리스트 삭제
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "IP 화이트리스트 삭제", description = "IP 화이트리스트를 삭제합니다")
    public ResponseEntity<Void> deleteWhitelist(@PathVariable Long id) {
        if (!ipWhitelistRepository.existsById(id)) {
            throw new IllegalArgumentException("IP 화이트리스트를 찾을 수 없습니다: " + id);
        }

        ipWhitelistRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * IP 화이트리스트 활성화/비활성화
     */
    @PatchMapping("/{id}/toggle")
    @Operation(summary = "IP 화이트리스트 활성화/비활성화", description = "IP 화이트리스트를 활성화 또는 비활성화합니다")
    public ResponseEntity<IpWhitelistResponse> toggleWhitelist(@PathVariable Long id) {
        IpWhitelistEntity entity = ipWhitelistRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("IP 화이트리스트를 찾을 수 없습니다: " + id));

        entity.setEnabled(!entity.getEnabled());
        IpWhitelistEntity updated = ipWhitelistRepository.save(entity);

        return ResponseEntity.ok(IpWhitelistResponse.from(updated));
    }
}
