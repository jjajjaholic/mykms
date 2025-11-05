"""Basic Auth 인증"""

import os
import secrets
from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPBasic, HTTPBasicCredentials
from dotenv import load_dotenv

load_dotenv()

security = HTTPBasic()


def get_admin_credentials() -> tuple[str, str]:
    """환경변수에서 관리자 인증 정보 가져오기

    Returns:
        (username, password) 튜플
    """
    username = os.getenv('ADMIN_USERNAME', 'admin')
    password = os.getenv('ADMIN_PASSWORD', 'admin123')
    return username, password


def verify_credentials(credentials: HTTPBasicCredentials = Depends(security)) -> str:
    """Basic Auth 인증 확인

    Args:
        credentials: HTTP Basic 인증 정보

    Returns:
        인증된 사용자명

    Raises:
        HTTPException: 인증 실패 시
    """
    admin_username, admin_password = get_admin_credentials()

    # Timing attack 방지를 위해 secrets.compare_digest 사용
    is_correct_username = secrets.compare_digest(
        credentials.username.encode("utf8"),
        admin_username.encode("utf8")
    )
    is_correct_password = secrets.compare_digest(
        credentials.password.encode("utf8"),
        admin_password.encode("utf8")
    )

    if not (is_correct_username and is_correct_password):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="인증에 실패했습니다",
            headers={"WWW-Authenticate": "Basic"},
        )

    return credentials.username
