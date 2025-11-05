# MyKMS - 대칭키 관리 시스템

대칭키(Symmetric Key)를 관리하는 KMS(Key Management System)입니다.

## 기능

- **인증**: Basic Auth를 사용한 API 인증
- **키 생성**: 새로운 대칭키 생성
- **키 저장**: 생성된 키를 안전하게 저장
- **키 요청**: 저장된 키 조회
- **키 업데이트**: 기존 키 업데이트

## 설치

```bash
# 가상환경 생성
python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate

# 의존성 설치
pip install -r requirements.txt

# 환경변수 설정
cp .env.example .env
# .env 파일을 수정하여 인증 정보를 설정하세요
```

## 실행

```bash
python main.py
```

서버는 기본적으로 `http://localhost:8000`에서 실행됩니다.

## API 문서

서버 실행 후 다음 URL에서 자동 생성된 API 문서를 확인할 수 있습니다:
- Swagger UI: http://localhost:8000/docs
- ReDoc: http://localhost:8000/redoc

## API 엔드포인트

### 인증
모든 API는 Basic Auth를 사용합니다. 요청 시 헤더에 인증 정보를 포함해야 합니다.

```bash
Authorization: Basic <base64_encoded_credentials>
```

### 키 생성
```
POST /api/keys
```

### 키 조회
```
GET /api/keys/{key_id}
```

### 키 목록
```
GET /api/keys
```

### 키 업데이트
```
PUT /api/keys/{key_id}
```

## 보안

- 모든 키는 암호화되어 저장됩니다
- Basic Auth를 통한 인증이 필요합니다
- 프로덕션 환경에서는 HTTPS를 사용하세요
