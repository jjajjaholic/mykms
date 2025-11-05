package com.mykms.service;

import com.mykms.dto.BackupListResponse;
import com.mykms.dto.BackupResponse;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 백업 및 복원 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BackupService {

    private final EntityManager entityManager;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${mykms.backup.directory:backups}")
    private String backupDirectory;

    /**
     * 데이터베이스 백업 생성
     */
    @Transactional(readOnly = true)
    public BackupResponse createBackup() throws IOException {
        // 백업 디렉토리 생성
        Path backupDir = Paths.get(backupDirectory);
        if (!Files.exists(backupDir)) {
            Files.createDirectories(backupDir);
            log.info("백업 디렉토리 생성: {}", backupDir.toAbsolutePath());
        }

        // 데이터베이스 파일 경로 추출
        String dbPath = extractDbPath(datasourceUrl);
        Path sourceDb = Paths.get(dbPath);

        if (!Files.exists(sourceDb)) {
            throw new IOException("데이터베이스 파일을 찾을 수 없습니다: " + dbPath);
        }

        // 백업 파일명 생성 (timestamp 포함)
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String backupFileName = "backup_" + timestamp + ".db";
        Path backupFile = backupDir.resolve(backupFileName);

        // 데이터베이스 파일 복사
        Files.copy(sourceDb, backupFile, StandardCopyOption.REPLACE_EXISTING);

        // WAL 파일이 있다면 함께 백업
        Path walFile = Paths.get(dbPath + "-wal");
        if (Files.exists(walFile)) {
            Path backupWal = backupDir.resolve(backupFileName + "-wal");
            Files.copy(walFile, backupWal, StandardCopyOption.REPLACE_EXISTING);
        }

        // SHM 파일이 있다면 함께 백업
        Path shmFile = Paths.get(dbPath + "-shm");
        if (Files.exists(shmFile)) {
            Path backupShm = backupDir.resolve(backupFileName + "-shm");
            Files.copy(shmFile, backupShm, StandardCopyOption.REPLACE_EXISTING);
        }

        long fileSize = Files.size(backupFile);
        LocalDateTime createdAt = LocalDateTime.ofInstant(
                Files.readAttributes(backupFile, BasicFileAttributes.class).creationTime().toInstant(),
                ZoneId.systemDefault()
        );

        log.info("백업 생성 완료: {} ({}바이트)", backupFileName, fileSize);

        return BackupResponse.builder()
                .filename(backupFileName)
                .filepath(backupFile.toAbsolutePath().toString())
                .size(fileSize)
                .createdAt(createdAt)
                .message("백업이 성공적으로 생성되었습니다")
                .build();
    }

    /**
     * 백업 파일 목록 조회
     */
    public BackupListResponse listBackups() throws IOException {
        Path backupDir = Paths.get(backupDirectory);

        if (!Files.exists(backupDir)) {
            return BackupListResponse.builder()
                    .backups(new ArrayList<>())
                    .total(0)
                    .build();
        }

        List<BackupListResponse.BackupFileInfo> backups = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(backupDir, "*.db")) {
            for (Path file : stream) {
                BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);
                LocalDateTime createdAt = LocalDateTime.ofInstant(
                        attrs.creationTime().toInstant(),
                        ZoneId.systemDefault()
                );

                backups.add(BackupListResponse.BackupFileInfo.builder()
                        .filename(file.getFileName().toString())
                        .size(attrs.size())
                        .createdAt(createdAt)
                        .build());
            }
        }

        // 생성 시간 역순으로 정렬
        backups.sort(Comparator.comparing(BackupListResponse.BackupFileInfo::getCreatedAt).reversed());

        return BackupListResponse.builder()
                .backups(backups)
                .total(backups.size())
                .build();
    }

    /**
     * 백업에서 데이터베이스 복원
     */
    @Transactional
    public BackupResponse restoreFromBackup(String backupFileName) throws IOException {
        Path backupDir = Paths.get(backupDirectory);
        Path backupFile = backupDir.resolve(backupFileName);

        if (!Files.exists(backupFile)) {
            throw new IOException("백업 파일을 찾을 수 없습니다: " + backupFileName);
        }

        // 현재 데이터베이스 파일 경로
        String dbPath = extractDbPath(datasourceUrl);
        Path currentDb = Paths.get(dbPath);

        // 현재 데이터베이스를 임시 백업
        Path tempBackup = Paths.get(dbPath + ".temp_backup");
        if (Files.exists(currentDb)) {
            Files.copy(currentDb, tempBackup, StandardCopyOption.REPLACE_EXISTING);
            log.info("현재 데이터베이스 임시 백업 완료");
        }

        try {
            // EntityManager 초기화
            entityManager.clear();

            // 백업 파일로 복원
            Files.copy(backupFile, currentDb, StandardCopyOption.REPLACE_EXISTING);

            // WAL 파일 복원
            Path backupWal = backupDir.resolve(backupFileName + "-wal");
            if (Files.exists(backupWal)) {
                Path walFile = Paths.get(dbPath + "-wal");
                Files.copy(backupWal, walFile, StandardCopyOption.REPLACE_EXISTING);
            }

            // SHM 파일 복원
            Path backupShm = backupDir.resolve(backupFileName + "-shm");
            if (Files.exists(backupShm)) {
                Path shmFile = Paths.get(dbPath + "-shm");
                Files.copy(backupShm, shmFile, StandardCopyOption.REPLACE_EXISTING);
            }

            // 임시 백업 삭제
            Files.deleteIfExists(tempBackup);

            log.info("백업 복원 완료: {}", backupFileName);

            return BackupResponse.builder()
                    .filename(backupFileName)
                    .filepath(currentDb.toAbsolutePath().toString())
                    .size(Files.size(currentDb))
                    .createdAt(LocalDateTime.now())
                    .message("백업이 성공적으로 복원되었습니다. 애플리케이션을 재시작해야 변경사항이 완전히 적용됩니다.")
                    .build();

        } catch (IOException e) {
            // 복원 실패 시 임시 백업으로 롤백
            if (Files.exists(tempBackup)) {
                Files.copy(tempBackup, currentDb, StandardCopyOption.REPLACE_EXISTING);
                Files.deleteIfExists(tempBackup);
                log.error("복원 실패, 이전 상태로 롤백 완료");
            }
            throw new IOException("백업 복원 중 오류 발생: " + e.getMessage(), e);
        }
    }

    /**
     * 백업 파일 다운로드용 리소스 반환
     */
    public Resource loadBackupAsResource(String filename) throws IOException {
        try {
            Path backupDir = Paths.get(backupDirectory);
            Path file = backupDir.resolve(filename).normalize();

            // 경로 탐색 공격 방지
            if (!file.startsWith(backupDir)) {
                throw new IOException("잘못된 파일 경로입니다");
            }

            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new IOException("파일을 읽을 수 없습니다: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new IOException("파일을 찾을 수 없습니다: " + filename, e);
        }
    }

    /**
     * JDBC URL에서 데이터베이스 파일 경로 추출
     */
    private String extractDbPath(String jdbcUrl) {
        // jdbc:sqlite:keys_storage.db -> keys_storage.db
        return jdbcUrl.replace("jdbc:sqlite:", "");
    }
}
