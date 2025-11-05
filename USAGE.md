# MyKMS 사용 가이드

## 설치 및 실행

### 1. 의존성 설치

```bash
# 가상환경 생성 (선택사항)
python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate

# 의존성 설치
pip install -r requirements.txt
```

### 2. 환경 설정

`.env` 파일을 수정하여 인증 정보를 설정합니다:

```bash
# Basic Auth Credentials
ADMIN_USERNAME=admin
ADMIN_PASSWORD=your_secure_password_here

# Server Configuration
HOST=0.0.0.0
PORT=8000

# Key Storage
KEYS_STORAGE_PATH=./keys_storage.db
```

### 3. 서버 실행

```bash
python main.py
```

서버가 시작되면 다음 URL에서 API 문서를 확인할 수 있습니다:
- Swagger UI: http://localhost:8000/docs
- ReDoc: http://localhost:8000/redoc

## API 사용 예제

### 1. 키 생성

새로운 대칭키를 생성합니다.

```bash
curl -X POST "http://localhost:8000/api/keys" \
  -u "admin:admin123" \
  -H "Content-Type: application/json" \
  -d '{
    "key_name": "my-encryption-key",
    "key_size": 256,
    "description": "데이터 암호화를 위한 대칭키"
  }'
```

**응답 예시:**
```json
{
  "key_id": "123e4567-e89b-12d3-a456-426614174000",
  "key_name": "my-encryption-key",
  "key_value": "SGVsbG8gV29ybGQ...",
  "key_size": 256,
  "description": "데이터 암호화를 위한 대칭키",
  "created_at": "2025-01-01T00:00:00",
  "updated_at": "2025-01-01T00:00:00"
}
```

### 2. 키 목록 조회

저장된 모든 키의 목록을 조회합니다 (키 값 제외).

```bash
curl -X GET "http://localhost:8000/api/keys" \
  -u "admin:admin123"
```

**응답 예시:**
```json
[
  {
    "key_id": "123e4567-e89b-12d3-a456-426614174000",
    "key_name": "my-encryption-key",
    "key_size": 256,
    "description": "데이터 암호화를 위한 대칭키",
    "created_at": "2025-01-01T00:00:00",
    "updated_at": "2025-01-01T00:00:00"
  }
]
```

### 3. 키 조회 (ID로)

특정 키의 상세 정보를 조회합니다 (키 값 포함).

```bash
curl -X GET "http://localhost:8000/api/keys/{key_id}" \
  -u "admin:admin123"
```

### 4. 키 조회 (이름으로)

키 이름으로 키를 조회합니다.

```bash
curl -X GET "http://localhost:8000/api/keys/name/my-encryption-key" \
  -u "admin:admin123"
```

### 5. 키 업데이트

키의 메타데이터를 업데이트합니다.

```bash
curl -X PUT "http://localhost:8000/api/keys/{key_id}" \
  -u "admin:admin123" \
  -H "Content-Type: application/json" \
  -d '{
    "key_name": "updated-key-name",
    "description": "업데이트된 설명",
    "rotate": false
  }'
```

### 6. 키 로테이션

새로운 키 값을 생성합니다 (키 로테이션).

```bash
curl -X PUT "http://localhost:8000/api/keys/{key_id}" \
  -u "admin:admin123" \
  -H "Content-Type: application/json" \
  -d '{
    "rotate": true
  }'
```

### 7. 키 삭제

특정 키를 삭제합니다.

```bash
curl -X DELETE "http://localhost:8000/api/keys/{key_id}" \
  -u "admin:admin123"
```

## Python 클라이언트 예제

```python
import requests
from requests.auth import HTTPBasicAuth
import base64

# 기본 설정
BASE_URL = "http://localhost:8000"
auth = HTTPBasicAuth("admin", "admin123")

# 1. 키 생성
response = requests.post(
    f"{BASE_URL}/api/keys",
    auth=auth,
    json={
        "key_name": "my-encryption-key",
        "key_size": 256,
        "description": "데이터 암호화를 위한 대칭키"
    }
)
key_info = response.json()
key_id = key_info["key_id"]
key_value = key_info["key_value"]

print(f"키 생성됨: {key_id}")
print(f"키 값 (Base64): {key_value}")

# Base64 디코딩하여 실제 키 값 얻기
key_bytes = base64.b64decode(key_value)
print(f"키 크기: {len(key_bytes)} bytes")

# 2. 키 목록 조회
response = requests.get(f"{BASE_URL}/api/keys", auth=auth)
keys = response.json()
print(f"저장된 키 개수: {len(keys)}")

# 3. 키 조회
response = requests.get(f"{BASE_URL}/api/keys/{key_id}", auth=auth)
key_info = response.json()
print(f"조회된 키: {key_info['key_name']}")

# 4. 키 업데이트
response = requests.put(
    f"{BASE_URL}/api/keys/{key_id}",
    auth=auth,
    json={
        "description": "업데이트된 설명"
    }
)
updated_key = response.json()
print(f"업데이트된 키: {updated_key['description']}")

# 5. 키 로테이션
response = requests.put(
    f"{BASE_URL}/api/keys/{key_id}",
    auth=auth,
    json={
        "rotate": True
    }
)
rotated_key = response.json()
print(f"로테이션된 키 값: {rotated_key['key_value']}")

# 6. 키 삭제
response = requests.delete(f"{BASE_URL}/api/keys/{key_id}", auth=auth)
if response.status_code == 204:
    print("키 삭제됨")
```

## 보안 권장사항

1. **프로덕션 환경에서는 반드시 HTTPS를 사용하세요**
   - HTTP를 사용하면 Basic Auth 인증 정보와 키 값이 평문으로 전송됩니다

2. **강력한 비밀번호 사용**
   - `.env` 파일의 `ADMIN_PASSWORD`를 강력한 비밀번호로 변경하세요
   - 최소 16자 이상의 무작위 문자열을 권장합니다

3. **데이터베이스 파일 보호**
   - `keys_storage.db` 파일의 접근 권한을 제한하세요
   - 정기적으로 백업하세요

4. **환경변수 관리**
   - `.env` 파일을 Git에 커밋하지 마세요
   - 프로덕션 환경에서는 환경변수 관리 시스템을 사용하세요

5. **키 로테이션**
   - 정기적으로 키를 로테이션하여 보안을 강화하세요
   - 로테이션 주기는 보안 정책에 따라 설정하세요

## 테스트

제공된 테스트 스크립트를 사용하여 API를 테스트할 수 있습니다:

```bash
chmod +x test_api.sh
./test_api.sh
```

## 문제 해결

### 서버가 시작되지 않는 경우
1. 포트가 이미 사용 중인지 확인: `lsof -i :8000`
2. 의존성이 모두 설치되었는지 확인: `pip install -r requirements.txt`

### 인증 오류
1. `.env` 파일의 인증 정보가 올바른지 확인
2. Basic Auth 헤더가 올바른 형식인지 확인

### 키 저장 오류
1. 데이터베이스 파일 경로에 쓰기 권한이 있는지 확인
2. 디스크 공간이 충분한지 확인
