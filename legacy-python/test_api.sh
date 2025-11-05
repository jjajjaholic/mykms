#!/bin/bash

# MyKMS API 테스트 스크립트

# 기본 설정
BASE_URL="http://localhost:8000"
AUTH="admin:admin123"

echo "======================================"
echo "MyKMS API 테스트"
echo "======================================"
echo ""

# 1. 헬스 체크
echo "1. 헬스 체크"
curl -X GET "$BASE_URL/health"
echo -e "\n"

# 2. 키 생성
echo "2. 키 생성 - my-encryption-key"
RESPONSE=$(curl -s -X POST "$BASE_URL/api/keys" \
  -u "$AUTH" \
  -H "Content-Type: application/json" \
  -d '{
    "key_name": "my-encryption-key",
    "key_size": 256,
    "description": "테스트용 대칭키"
  }')
echo "$RESPONSE" | python3 -m json.tool
KEY_ID=$(echo "$RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin)['key_id'])" 2>/dev/null)
echo -e "\n"

# 3. 키 목록 조회
echo "3. 키 목록 조회"
curl -s -X GET "$BASE_URL/api/keys" \
  -u "$AUTH" | python3 -m json.tool
echo -e "\n"

# 4. 특정 키 조회 (ID로)
if [ ! -z "$KEY_ID" ]; then
  echo "4. 키 조회 (ID: $KEY_ID)"
  curl -s -X GET "$BASE_URL/api/keys/$KEY_ID" \
    -u "$AUTH" | python3 -m json.tool
  echo -e "\n"
fi

# 5. 키 조회 (이름으로)
echo "5. 키 조회 (이름: my-encryption-key)"
curl -s -X GET "$BASE_URL/api/keys/name/my-encryption-key" \
  -u "$AUTH" | python3 -m json.tool
echo -e "\n"

# 6. 키 업데이트
if [ ! -z "$KEY_ID" ]; then
  echo "6. 키 업데이트 (설명 변경)"
  curl -s -X PUT "$BASE_URL/api/keys/$KEY_ID" \
    -u "$AUTH" \
    -H "Content-Type: application/json" \
    -d '{
      "description": "업데이트된 테스트용 대칭키",
      "rotate": false
    }' | python3 -m json.tool
  echo -e "\n"
fi

# 7. 키 로테이션
if [ ! -z "$KEY_ID" ]; then
  echo "7. 키 로테이션 (새로운 키 값 생성)"
  curl -s -X PUT "$BASE_URL/api/keys/$KEY_ID" \
    -u "$AUTH" \
    -H "Content-Type: application/json" \
    -d '{
      "rotate": true
    }' | python3 -m json.tool
  echo -e "\n"
fi

# 8. 인증 실패 테스트
echo "8. 인증 실패 테스트"
curl -s -X GET "$BASE_URL/api/keys" \
  -u "wrong:credentials" | python3 -m json.tool
echo -e "\n"

# 9. 키 삭제
if [ ! -z "$KEY_ID" ]; then
  echo "9. 키 삭제 (ID: $KEY_ID)"
  curl -s -X DELETE "$BASE_URL/api/keys/$KEY_ID" \
    -u "$AUTH" \
    -w "\nHTTP Status: %{http_code}\n"
  echo -e "\n"
fi

# 10. 삭제 후 목록 확인
echo "10. 삭제 후 키 목록 조회"
curl -s -X GET "$BASE_URL/api/keys" \
  -u "$AUTH" | python3 -m json.tool
echo -e "\n"

echo "======================================"
echo "테스트 완료"
echo "======================================"
