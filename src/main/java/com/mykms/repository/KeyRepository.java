package com.mykms.repository;

import com.mykms.entity.KeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 키 Repository
 */
@Repository
public interface KeyRepository extends JpaRepository<KeyEntity, String> {

    /**
     * 키 이름으로 키 조회
     *
     * @param keyName 키 이름
     * @return 키 엔티티
     */
    Optional<KeyEntity> findByKeyName(String keyName);

    /**
     * 모든 키 목록 조회 (생성 시간 역순)
     *
     * @return 키 엔티티 리스트
     */
    List<KeyEntity> findAllByOrderByCreatedAtDesc();
}
