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
│       │   │   └── GlobalExceptionHandler.java
│       │   ├── dto/                       # 데이터 전송 객체
│       │   ├── entity/                    # JPA 엔티티
│       │   ├── repository/                # JPA 리포지토리
│       │   └── service/                   # 비즈니스 로직
│       │       ├── CryptoService.java     # 암호화 서비스
│       │       └── KeyService.java        # 키 관리 서비스
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

## 환경변수

다음 환경변수를 설정하여 애플리케이션을 구성할 수 있습니다:

- `MYKMS_ADMIN_USERNAME`: 관리자 사용자명 (기본값: `admin`)
- `MYKMS_ADMIN_PASSWORD`: 관리자 비밀번호 (기본값: `admin123`)
- `MYKMS_MASTER_PASSWORD`: 마스터 암호화 비밀번호 (기본값: `admin123`)
- `SERVER_PORT`: 서버 포트 (기본값: `8080`)

## 라이선스

MIT
