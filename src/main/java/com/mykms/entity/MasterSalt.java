package com.mykms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 마스터 솔트 엔티티
 * 마스터 키 유도에 사용되는 솔트를 저장
 */
@Entity
@Table(name = "master_salt")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasterSalt {

    @Id
    @Column(nullable = false)
    private Integer id = 1; // 항상 1로 고정 (단일 레코드)

    @Lob
    @Column(nullable = false)
    private byte[] salt;
}
