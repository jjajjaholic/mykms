package com.mykms.repository;

import com.mykms.entity.MasterSalt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 마스터 솔트 Repository
 */
@Repository
public interface MasterSaltRepository extends JpaRepository<MasterSalt, Integer> {
}
