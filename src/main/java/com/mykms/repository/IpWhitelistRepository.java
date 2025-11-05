package com.mykms.repository;

import com.mykms.entity.IpWhitelistEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * IP 화이트리스트 리포지토리
 */
@Repository
public interface IpWhitelistRepository extends JpaRepository<IpWhitelistEntity, Long> {

    /**
     * IP 주소로 조회
     */
    Optional<IpWhitelistEntity> findByIpAddress(String ipAddress);

    /**
     * 활성화된 IP 목록 조회
     */
    List<IpWhitelistEntity> findByEnabledTrue();

    /**
     * IP 주소 존재 여부 확인
     */
    boolean existsByIpAddress(String ipAddress);
}
