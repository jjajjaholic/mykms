# MyKMS - 대칭키 관리 시스템 (Spring Boot)

대칭키(Symmetric Key)를 관리하는 KMS(Key Management System)입니다.

**이 프로젝트는 Spring Boot 3.2.0과 Java 17을 사용하여 구현되었습니다.**

## 기능

- **인증**: Basic Auth를 사용한 API 인증
- **키 생성**: 새로운 대칭키 생성
- **키 저장**: 생성된 키를 AES-256-GCM으로 암호화하여 안전하게 저장
- **키 요청**: 저장된 키 조회
- **키 업데이트**: 기존 키 정보 수정 및 로테이션
- **키 삭제**: 저장된 키 삭제
- **데이터베이스 백업**: SQLite 데이터베이스를 파일로 백업
- **데이터베이스 복원**: 백업 파일에서 데이터베이스 복원

## 기술 스택

- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Database**: SQLite with JPA/Hibernate
- **Security**: Spring Security (Basic Auth)
- **API Documentation**: SpringDoc OpenAPI (Swagger)
- **Build Tool**: Maven

## 요구사항

- Java 17 이상
- Maven 3.6 이상

## 설치 및 실행

### 1. 환경변수 설정 (선택사항)

```bash
cp .env.example .env
# .env 파일을 수정하여 인증 정보를 설정하세요
```

또는 `src/main/resources/application.properties` 파일을 직접 수정할 수 있습니다.

### 2. 빌드

```bash
mvn clean package
```

### 3. 실행

```bash
mvn spring-boot:run
```

또는 빌드된 JAR 파일을 직접 실행:

```bash
java -jar target/mykms-1.0.0.jar
```

서버는 기본적으로 `http://localhost:8080`에서 실행됩니다.

## API 문서

서버 실행 후 다음 URL에서 자동 생성된 API 문서를 확인할 수 있습니다:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/v3/api-docs

## API 엔드포인트

### 인증
모든 API는 Basic Auth를 사용합니다. 요청 시 헤더에 인증 정보를 포함해야 합니다.

```bash
Authorization: Basic <base64_encoded_credentials>
```

기본 인증 정보:
- Username: `admin`
- Password: `admin123`

### 키 생성
```
POST /api/keys
```

### 키 조회
```
GET /api/keys/{key_id}
```

### 이름으로 키 조회
```
GET /api/keys/name/{key_name}
```

### 키 목록
```
GET /api/keys
```

### 키 업데이트
```
PUT /api/keys/{key_id}
```

### 키 삭제
```
DELETE /api/keys/{key_id}
```

### 백업 생성
```
POST /api/backup
```

### 백업 목록 조회
```
GET /api/backup/list
```

### 백업 복원
```
POST /api/backup/restore
```

### 백업 다운로드
```
GET /api/backup/download/{filename}
```

## 프로젝트 구조

```
mykms/
├── src/
│   └── main/
│       ├── java/com/mykms/
│       │   ├── MyKmsApplication.java      # 메인 애플리케이션
│       │   ├── config/                    # 설정 클래스
│       │   │   ├── SecurityConfig.java    # Spring Security 설정
│       │   │   └── OpenApiConfig.java     # Swagger 설정
│       │   ├── controller/                # REST 컨트롤러
│       │   │   ├── RootController.java
│       │   │   ├── KeyController.java
│       │   │   ├── BackupController.java
│       │   │   └── GlobalExceptionHandler.java
│       │   ├── dto/                       # 데이터 전송 객체
│       │   ├── entity/                    # JPA 엔티티
│       │   │   ├── KeyEntity.java
│       │   │   └── MasterSalt.java
│       │   ├── repository/                # JPA 리포지토리
│       │   └── service/                   # 비즈니스 로직
│       │       ├── CryptoService.java     # 암호화 서비스
│       │       ├── KeyService.java        # 키 관리 서비스
│       │       └── BackupService.java     # 백업 서비스
│       └── resources/
│           └── application.properties     # 애플리케이션 설정
├── pom.xml                                # Maven 설정
└── README.md
```

## 보안

- 모든 키는 AES-256-GCM으로 암호화되어 저장됩니다
- 마스터 키는 PBKDF2를 사용하여 비밀번호로부터 유도됩니다 (100,000 iterations)
- Basic Auth를 통한 인증이 필요합니다
- **프로덕션 환경에서는 반드시 HTTPS를 사용하세요**
- **프로덕션 환경에서는 기본 비밀번호를 반드시 변경하세요**

## 데이터베이스 구조

MyKMS는 SQLite 데이터베이스를 사용하며, 다음 두 개의 테이블로 구성되어 있습니다.

### 1. keys 테이블

대칭키를 안전하게 암호화하여 저장하는 메인 테이블입니다.

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| key_id | VARCHAR(36) | PRIMARY KEY | 키의 고유 식별자 (UUID) |
| key_name | VARCHAR | NOT NULL, UNIQUE | 키의 이름 (중복 불가) |
| encrypted_key | BLOB | NOT NULL | AES-256-GCM으로 암호화된 대칭키 |
| iv | BLOB | NOT NULL | 암호화에 사용된 초기화 벡터 (Initialization Vector) |
| tag | BLOB | NOT NULL | GCM 모드 인증 태그 (Authentication Tag) |
| key_size | INTEGER | NOT NULL | 원본 키의 크기 (비트 단위: 128, 192, 256) |
| description | VARCHAR(1000) | NULL | 키에 대한 설명 |
| created_at | TIMESTAMP | NOT NULL | 키 생성 시간 (자동 생성) |
| updated_at | TIMESTAMP | NOT NULL | 키 수정 시간 (자동 업데이트) |

**특징:**
- 키는 AES-256-GCM 알고리즘으로 암호화되어 저장됩니다
- `iv`, `tag` 필드는 암호화/복호화에 필수적인 정보를 저장합니다
- `key_name`은 고유해야 하며, 키를 이름으로 조회할 수 있습니다
- 타임스탬프는 Hibernate 어노테이션을 통해 자동 관리됩니다

### 2. master_salt 테이블

마스터 키 유도에 사용되는 솔트(Salt)를 저장하는 테이블입니다.

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| id | INTEGER | PRIMARY KEY | 고정값 1 (단일 레코드) |
| salt | BLOB | NOT NULL | PBKDF2 키 유도에 사용되는 솔트 |

**특징:**
- 이 테이블은 단일 레코드만 가집니다 (id는 항상 1)
- 솔트는 마스터 비밀번호로부터 마스터 키를 안전하게 유도하는 데 사용됩니다
- PBKDF2 알고리즘과 함께 사용되어 100,000회의 iteration을 거쳐 마스터 키를 생성합니다

### 데이터베이스 파일

- **파일명**: `keys_storage.db`
- **위치**: 프로젝트 루트 디렉토리
- **백업**: `backups/` 디렉토리에 백업 파일 저장

### ERD (Entity Relationship Diagram)

```
┌─────────────────────────────────────┐
│            keys                      │
├─────────────────────────────────────┤
│ PK  key_id          VARCHAR(36)     │
│ UK  key_name        VARCHAR         │
│     encrypted_key   BLOB            │
│     iv              BLOB            │
│     tag             BLOB            │
│     key_size        INTEGER         │
│     description     VARCHAR(1000)   │
│     created_at      TIMESTAMP       │
│     updated_at      TIMESTAMP       │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│          master_salt                │
├─────────────────────────────────────┤
│ PK  id              INTEGER         │
│     salt            BLOB            │
└─────────────────────────────────────┘
```

## 백업 및 복원

MyKMS는 데이터베이스 백업 및 복원 기능을 제공합니다.

### 백업 생성
```bash
curl -X POST http://localhost:8080/api/backup -u admin:admin123
```

### 백업 목록 조회
```bash
curl -X GET http://localhost:8080/api/backup/list -u admin:admin123
```

### 백업 복원
```bash
curl -X POST http://localhost:8080/api/backup/restore \
  -H "Content-Type: application/json" \
  -d '{"filename": "backup_20250105_120000.db"}' \
  -u admin:admin123
```

### 백업 다운로드
```bash
curl -X GET http://localhost:8080/api/backup/download/backup_20250105_120000.db \
  -u admin:admin123 -O
```

**백업 파일 형식:**
- 파일명: `backup_YYYYMMDD_HHmmss.db`
- 위치: `backups/` 디렉토리
- 포함 내용: SQLite 데이터베이스 파일 및 WAL/SHM 파일

## 환경변수

다음 환경변수를 설정하여 애플리케이션을 구성할 수 있습니다:

- `MYKMS_ADMIN_USERNAME`: 관리자 사용자명 (기본값: `admin`)
- `MYKMS_ADMIN_PASSWORD`: 관리자 비밀번호 (기본값: `admin123`)
- `MYKMS_MASTER_PASSWORD`: 마스터 암호화 비밀번호 (기본값: `admin123`)
- `SERVER_PORT`: 서버 포트 (기본값: `8080`)

## 라이선스

MIT
