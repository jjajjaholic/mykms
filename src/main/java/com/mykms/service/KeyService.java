package com.mykms.service;

import com.mykms.dto.KeyCreateRequest;
import com.mykms.dto.KeyListResponse;
import com.mykms.dto.KeyResponse;
import com.mykms.dto.KeyUpdateRequest;
import com.mykms.entity.KeyEntity;
import com.mykms.entity.MasterSalt;
import com.mykms.repository.KeyRepository;
import com.mykms.repository.MasterSaltRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 키 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KeyService {

    private final KeyRepository keyRepository;
    private final MasterSaltRepository masterSaltRepository;
    private final CryptoService cryptoService;

    @Value("${mykms.master.password:admin123}")
    private String masterPassword;

    private byte[] masterKey;

    /**
     * 초기화: 마스터 솔트 생성 및 마스터 키 유도
     */
    @PostConstruct
    @Transactional
    public void init() {
        // 마스터 솔트 확인 및 생성
        MasterSalt masterSalt = masterSaltRepository.findById(1).orElseGet(() -> {
            log.info("마스터 솔트가 없습니다. 새로 생성합니다.");
            byte[] salt = new byte[32];
            new SecureRandom().nextBytes(salt);
            MasterSalt newSalt = MasterSalt.builder()
                    .id(1)
                    .salt(salt)
                    .build();
            return masterSaltRepository.save(newSalt);
        });

        // 마스터 키 유도
        this.masterKey = cryptoService.deriveEncryptionKey(masterPassword, masterSalt.getSalt());
        log.info("마스터 키가 성공적으로 유도되었습니다.");
    }

    /**
     * 새로운 키 생성
     *
     * @param request 키 생성 요청
     * @return 키 응답
     */
    @Transactional
    public KeyResponse createKey(KeyCreateRequest request) {
        // 이미 존재하는 키 이름인지 확인
        if (keyRepository.findByKeyName(request.getKeyName()).isPresent()) {
            throw new IllegalArgumentException("키 이름 '" + request.getKeyName() + "'이 이미 존재합니다");
        }

        // 키 생성
        String keyId = UUID.randomUUID().toString();
        byte[] keyData = cryptoService.generateSymmetricKey(request.getKeySize());

        // 마스터 키로 암호화
        CryptoService.EncryptedData encryptedData = cryptoService.encryptKey(keyData, masterKey);

        // 엔티티 생성 및 저장
        KeyEntity keyEntity = KeyEntity.builder()
                .keyId(keyId)
                .keyName(request.getKeyName())
                .encryptedKey(encryptedData.ciphertext())
                .iv(encryptedData.iv())
                .tag(encryptedData.tag())
                .keySize(request.getKeySize())
                .description(request.getDescription())
                .build();

        keyEntity = keyRepository.save(keyEntity);

        log.info("키 생성 완료: {}", keyId);

        return KeyResponse.builder()
                .keyId(keyEntity.getKeyId())
                .keyName(keyEntity.getKeyName())
                .keyValue(cryptoService.keyToBase64(keyData))
                .keySize(keyEntity.getKeySize())
                .description(keyEntity.getDescription())
                .createdAt(keyEntity.getCreatedAt())
                .updatedAt(keyEntity.getUpdatedAt())
                .build();
    }

    /**
     * 키 조회 (키 값 포함)
     *
     * @param keyId 키 ID
     * @return 키 응답
     */
    @Transactional(readOnly = true)
    public KeyResponse getKey(String keyId) {
        KeyEntity keyEntity = keyRepository.findById(keyId)
                .orElseThrow(() -> new IllegalArgumentException("키 ID '" + keyId + "'를 찾을 수 없습니다"));

        // 복호화
        byte[] keyData = cryptoService.decryptKey(
                keyEntity.getEncryptedKey(),
                masterKey,
                keyEntity.getIv(),
                keyEntity.getTag()
        );

        return KeyResponse.builder()
                .keyId(keyEntity.getKeyId())
                .keyName(keyEntity.getKeyName())
                .keyValue(cryptoService.keyToBase64(keyData))
                .keySize(keyEntity.getKeySize())
                .description(keyEntity.getDescription())
                .createdAt(keyEntity.getCreatedAt())
                .updatedAt(keyEntity.getUpdatedAt())
                .build();
    }

    /**
     * 이름으로 키 조회 (키 값 포함)
     *
     * @param keyName 키 이름
     * @return 키 응답
     */
    @Transactional(readOnly = true)
    public KeyResponse getKeyByName(String keyName) {
        KeyEntity keyEntity = keyRepository.findByKeyName(keyName)
                .orElseThrow(() -> new IllegalArgumentException("키 이름 '" + keyName + "'을 찾을 수 없습니다"));

        // 복호화
        byte[] keyData = cryptoService.decryptKey(
                keyEntity.getEncryptedKey(),
                masterKey,
                keyEntity.getIv(),
                keyEntity.getTag()
        );

        return KeyResponse.builder()
                .keyId(keyEntity.getKeyId())
                .keyName(keyEntity.getKeyName())
                .keyValue(cryptoService.keyToBase64(keyData))
                .keySize(keyEntity.getKeySize())
                .description(keyEntity.getDescription())
                .createdAt(keyEntity.getCreatedAt())
                .updatedAt(keyEntity.getUpdatedAt())
                .build();
    }

    /**
     * 모든 키 목록 조회 (키 값 제외)
     *
     * @return 키 목록 응답 리스트
     */
    @Transactional(readOnly = true)
    public List<KeyListResponse> listKeys() {
        return keyRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(keyEntity -> KeyListResponse.builder()
                        .keyId(keyEntity.getKeyId())
                        .keyName(keyEntity.getKeyName())
                        .keySize(keyEntity.getKeySize())
                        .description(keyEntity.getDescription())
                        .createdAt(keyEntity.getCreatedAt())
                        .updatedAt(keyEntity.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 키 업데이트
     *
     * @param keyId   키 ID
     * @param request 키 업데이트 요청
     * @return 키 응답
     */
    @Transactional
    public KeyResponse updateKey(String keyId, KeyUpdateRequest request) {
        KeyEntity keyEntity = keyRepository.findById(keyId)
                .orElseThrow(() -> new IllegalArgumentException("키 ID '" + keyId + "'를 찾을 수 없습니다"));

        // 키 이름 업데이트
        if (request.getKeyName() != null && !request.getKeyName().equals(keyEntity.getKeyName())) {
            // 중복 확인
            if (keyRepository.findByKeyName(request.getKeyName()).isPresent()) {
                throw new IllegalArgumentException("키 이름 '" + request.getKeyName() + "'이 이미 존재합니다");
            }
            keyEntity.setKeyName(request.getKeyName());
        }

        // 설명 업데이트
        if (request.getDescription() != null) {
            keyEntity.setDescription(request.getDescription());
        }

        byte[] keyData;

        // 키 로테이션
        if (Boolean.TRUE.equals(request.getRotate())) {
            log.info("키 로테이션 시작: {}", keyId);
            keyData = cryptoService.generateSymmetricKey(keyEntity.getKeySize());
            CryptoService.EncryptedData encryptedData = cryptoService.encryptKey(keyData, masterKey);

            keyEntity.setEncryptedKey(encryptedData.ciphertext());
            keyEntity.setIv(encryptedData.iv());
            keyEntity.setTag(encryptedData.tag());
        } else {
            // 기존 키 복호화
            keyData = cryptoService.decryptKey(
                    keyEntity.getEncryptedKey(),
                    masterKey,
                    keyEntity.getIv(),
                    keyEntity.getTag()
            );
        }

        keyEntity = keyRepository.save(keyEntity);

        log.info("키 업데이트 완료: {}", keyId);

        return KeyResponse.builder()
                .keyId(keyEntity.getKeyId())
                .keyName(keyEntity.getKeyName())
                .keyValue(cryptoService.keyToBase64(keyData))
                .keySize(keyEntity.getKeySize())
                .description(keyEntity.getDescription())
                .createdAt(keyEntity.getCreatedAt())
                .updatedAt(keyEntity.getUpdatedAt())
                .build();
    }

    /**
     * 키 삭제
     *
     * @param keyId 키 ID
     */
    @Transactional
    public void deleteKey(String keyId) {
        if (!keyRepository.existsById(keyId)) {
            throw new IllegalArgumentException("키 ID '" + keyId + "'를 찾을 수 없습니다");
        }

        keyRepository.deleteById(keyId);
        log.info("키 삭제 완료: {}", keyId);
    }
}
