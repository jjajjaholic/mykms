package com.mykms.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * 암호화 관련 서비스
 */
@Slf4j
@Service
public class CryptoService {

    private static final String AES_ALGORITHM = "AES";
    private static final String AES_GCM_MODE = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;
    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int PBKDF2_ITERATIONS = 100000;
    private static final int PBKDF2_KEY_LENGTH = 256;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * 대칭키 생성
     *
     * @param keySize 키 크기 (비트)
     * @return 생성된 대칭키 (바이트 배열)
     */
    public byte[] generateSymmetricKey(int keySize) {
        int keyBytes = keySize / 8;
        byte[] key = new byte[keyBytes];
        secureRandom.nextBytes(key);
        return key;
    }

    /**
     * 키를 Base64로 인코딩
     *
     * @param key 바이트 형태의 키
     * @return Base64 인코딩된 키 문자열
     */
    public String keyToBase64(byte[] key) {
        return Base64.getEncoder().encodeToString(key);
    }

    /**
     * Base64 문자열을 키로 디코딩
     *
     * @param keyB64 Base64 인코딩된 키 문자열
     * @return 바이트 형태의 키
     */
    public byte[] base64ToKey(String keyB64) {
        return Base64.getDecoder().decode(keyB64);
    }

    /**
     * 비밀번호로부터 암호화 키 유도
     *
     * @param password 비밀번호
     * @param salt     솔트
     * @return 유도된 암호화 키
     */
    public byte[] deriveEncryptionKey(String password, byte[] salt) {
        try {
            KeySpec spec = new PBEKeySpec(
                    password.toCharArray(),
                    salt,
                    PBKDF2_ITERATIONS,
                    PBKDF2_KEY_LENGTH
            );
            SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
            return factory.generateSecret(spec).getEncoded();
        } catch (Exception e) {
            log.error("Failed to derive encryption key", e);
            throw new RuntimeException("암호화 키 유도 실패", e);
        }
    }

    /**
     * 키 데이터를 마스터 키로 암호화
     *
     * @param keyData   암호화할 키 데이터
     * @param masterKey 마스터 키
     * @return EncryptedData 객체 (암호화된 데이터, IV, 태그 포함)
     */
    public EncryptedData encryptKey(byte[] keyData, byte[] masterKey) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            SecretKeySpec keySpec = new SecretKeySpec(masterKey, AES_ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

            Cipher cipher = Cipher.getInstance(AES_GCM_MODE);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);

            byte[] ciphertext = cipher.doFinal(keyData);

            // GCM 모드에서 ciphertext는 데이터 + 태그를 포함
            // 태그를 분리
            int ciphertextLength = ciphertext.length - (GCM_TAG_LENGTH / 8);
            byte[] encryptedData = new byte[ciphertextLength];
            byte[] tag = new byte[GCM_TAG_LENGTH / 8];

            System.arraycopy(ciphertext, 0, encryptedData, 0, ciphertextLength);
            System.arraycopy(ciphertext, ciphertextLength, tag, 0, tag.length);

            return new EncryptedData(encryptedData, iv, tag);
        } catch (Exception e) {
            log.error("Failed to encrypt key", e);
            throw new RuntimeException("키 암호화 실패", e);
        }
    }

    /**
     * 암호화된 키 데이터를 복호화
     *
     * @param ciphertext 암호화된 데이터
     * @param masterKey  마스터 키
     * @param iv         IV
     * @param tag        인증 태그
     * @return 복호화된 키 데이터
     */
    public byte[] decryptKey(byte[] ciphertext, byte[] masterKey, byte[] iv, byte[] tag) {
        try {
            // 암호문과 태그를 결합
            byte[] ciphertextWithTag = new byte[ciphertext.length + tag.length];
            System.arraycopy(ciphertext, 0, ciphertextWithTag, 0, ciphertext.length);
            System.arraycopy(tag, 0, ciphertextWithTag, ciphertext.length, tag.length);

            SecretKeySpec keySpec = new SecretKeySpec(masterKey, AES_ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

            Cipher cipher = Cipher.getInstance(AES_GCM_MODE);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);

            return cipher.doFinal(ciphertextWithTag);
        } catch (Exception e) {
            log.error("Failed to decrypt key", e);
            throw new RuntimeException("키 복호화 실패", e);
        }
    }

    /**
     * 암호화된 데이터를 담는 클래스
     */
    public record EncryptedData(byte[] ciphertext, byte[] iv, byte[] tag) {
    }
}
