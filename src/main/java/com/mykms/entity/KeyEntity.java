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
 * 키 엔티티
 */
@Entity
@Table(name = "keys")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeyEntity {

    @Id
    @Column(name = "key_id", nullable = false, length = 36)
    private String keyId;

    @Column(name = "key_name", nullable = false, unique = true)
    private String keyName;

    @Lob
    @Column(name = "encrypted_key", nullable = false)
    private byte[] encryptedKey;

    @Lob
    @Column(name = "iv", nullable = false)
    private byte[] iv;

    @Lob
    @Column(name = "tag", nullable = false)
    private byte[] tag;

    @Column(name = "key_size", nullable = false)
    private Integer keySize;

    @Column(name = "description", length = 1000)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
