package com.mykms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * IP 화이트리스트 엔티티
 * 허가된 IP 주소를 관리
 */
@Entity
@Table(name = "ip_whitelist")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IpWhitelistEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * IP 주소 (CIDR 표기법 지원)
     * 예: "192.168.1.100" 또는 "192.168.1.0/24"
     */
    @Column(nullable = false, unique = true)
    private String ipAddress;

    /**
     * 설명
     */
    @Column(length = 1000)
    private String description;

    /**
     * 활성화 여부
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    /**
     * 생성 시간
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정 시간
     */
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
