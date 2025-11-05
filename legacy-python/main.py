"""MyKMS - 대칭키 관리 시스템 메인 애플리케이션"""

import os
import uvicorn
from fastapi import FastAPI
from fastapi.responses import JSONResponse
from dotenv import load_dotenv
from app.api.routes import router as api_router

# 환경변수 로드
load_dotenv()

# FastAPI 애플리케이션 생성
app = FastAPI(
    title="MyKMS - Key Management System",
    description="""
    대칭키(Symmetric Key)를 안전하게 관리하는 KMS(Key Management System)입니다.

    ## 기능

    - **인증**: Basic Auth를 사용한 API 인증
    - **키 생성**: 새로운 대칭키 생성
    - **키 저장**: 생성된 키를 암호화하여 안전하게 저장
    - **키 요청**: 저장된 키 조회
    - **키 업데이트**: 기존 키 정보 수정 및 로테이션
    - **키 삭제**: 저장된 키 삭제

    ## 인증

    모든 API는 Basic Auth를 사용합니다. 요청 시 헤더에 인증 정보를 포함해야 합니다:

    ```
    Authorization: Basic <base64_encoded_credentials>
    ```

    기본 인증 정보:
    - Username: admin
    - Password: admin123 (환경변수에서 변경 가능)

    ## 보안

    - 모든 키는 AES-256-GCM으로 암호화되어 저장됩니다
    - 마스터 키는 PBKDF2를 사용하여 비밀번호로부터 유도됩니다
    - 프로덕션 환경에서는 반드시 HTTPS를 사용하세요
    """,
    version="1.0.0",
    contact={
        "name": "MyKMS",
        "url": "https://github.com/yourusername/mykms",
    },
    license_info={
        "name": "MIT",
    },
)

# API 라우터 등록
app.include_router(api_router)


@app.get("/", tags=["root"])
async def root():
    """루트 엔드포인트"""
    return {
        "message": "MyKMS - Key Management System",
        "version": "1.0.0",
        "docs": "/docs",
        "redoc": "/redoc"
    }


@app.get("/health", tags=["health"])
async def health_check():
    """헬스 체크 엔드포인트"""
    return {
        "status": "healthy",
        "service": "MyKMS"
    }


def main():
    """애플리케이션 실행"""
    host = os.getenv("HOST", "0.0.0.0")
    port = int(os.getenv("PORT", "8000"))

    print(f"""
    ╔═══════════════════════════════════════════════════════════════╗
    ║                                                               ║
    ║   MyKMS - Key Management System                              ║
    ║                                                               ║
    ║   서버 시작됨: http://{host}:{port}                     ║
    ║                                                               ║
    ║   API 문서:                                                   ║
    ║   - Swagger UI: http://{host}:{port}/docs                ║
    ║   - ReDoc:      http://{host}:{port}/redoc               ║
    ║                                                               ║
    ║   인증 정보 (Basic Auth):                                     ║
    ║   - Username: {os.getenv('ADMIN_USERNAME', 'admin')}         ║
    ║   - Password: {os.getenv('ADMIN_PASSWORD', 'admin123')}      ║
    ║                                                               ║
    ╚═══════════════════════════════════════════════════════════════╝
    """)

    uvicorn.run(
        app,
        host=host,
        port=port,
        log_level="info"
    )


if __name__ == "__main__":
    main()
