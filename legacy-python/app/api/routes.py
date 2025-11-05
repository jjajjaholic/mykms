"""API 라우트"""

from fastapi import APIRouter, Depends, HTTPException, status
from typing import List
from app.models import (
    KeyCreateRequest,
    KeyUpdateRequest,
    KeyResponse,
    KeyListResponse,
    ErrorResponse
)
from app.auth import verify_credentials
from app.database import KeyStore
import os
from dotenv import load_dotenv

load_dotenv()

router = APIRouter(prefix="/api", tags=["keys"])

# KeyStore 인스턴스 (싱글톤 패턴)
_key_store = None


def get_key_store() -> KeyStore:
    """KeyStore 인스턴스 가져오기"""
    global _key_store
    if _key_store is None:
        db_path = os.getenv('KEYS_STORAGE_PATH', './keys_storage.db')
        master_password = os.getenv('ADMIN_PASSWORD', 'admin123')
        _key_store = KeyStore(db_path, master_password)
    return _key_store


@router.post(
    "/keys",
    response_model=KeyResponse,
    status_code=status.HTTP_201_CREATED,
    summary="키 생성",
    description="새로운 대칭키를 생성합니다",
    responses={
        201: {"description": "키 생성 성공"},
        400: {"model": ErrorResponse, "description": "잘못된 요청"},
        401: {"model": ErrorResponse, "description": "인증 실패"},
        409: {"model": ErrorResponse, "description": "이미 존재하는 키 이름"}
    }
)
async def create_key(
    request: KeyCreateRequest,
    username: str = Depends(verify_credentials)
):
    """새로운 대칭키 생성"""
    key_store = get_key_store()

    # 이미 존재하는 키 이름인지 확인
    existing_key = key_store.get_key_by_name(request.key_name)
    if existing_key:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail=f"키 이름 '{request.key_name}'이 이미 존재합니다"
        )

    try:
        key_info = key_store.create_key(
            key_name=request.key_name,
            key_size=request.key_size,
            description=request.description
        )
        return key_info
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"키 생성 중 오류가 발생했습니다: {str(e)}"
        )


@router.get(
    "/keys",
    response_model=List[KeyListResponse],
    summary="키 목록 조회",
    description="저장된 모든 키의 목록을 조회합니다 (키 값 제외)",
    responses={
        200: {"description": "키 목록 조회 성공"},
        401: {"model": ErrorResponse, "description": "인증 실패"}
    }
)
async def list_keys(username: str = Depends(verify_credentials)):
    """저장된 모든 키 목록 조회"""
    key_store = get_key_store()

    try:
        keys = key_store.list_keys()
        return keys
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"키 목록 조회 중 오류가 발생했습니다: {str(e)}"
        )


@router.get(
    "/keys/{key_id}",
    response_model=KeyResponse,
    summary="키 조회",
    description="특정 키의 정보를 조회합니다 (키 값 포함)",
    responses={
        200: {"description": "키 조회 성공"},
        401: {"model": ErrorResponse, "description": "인증 실패"},
        404: {"model": ErrorResponse, "description": "키를 찾을 수 없음"}
    }
)
async def get_key(key_id: str, username: str = Depends(verify_credentials)):
    """특정 키 조회"""
    key_store = get_key_store()

    try:
        key_info = key_store.get_key(key_id)
        if key_info is None:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"키 ID '{key_id}'를 찾을 수 없습니다"
            )
        return key_info
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"키 조회 중 오류가 발생했습니다: {str(e)}"
        )


@router.get(
    "/keys/name/{key_name}",
    response_model=KeyResponse,
    summary="이름으로 키 조회",
    description="키 이름으로 키를 조회합니다 (키 값 포함)",
    responses={
        200: {"description": "키 조회 성공"},
        401: {"model": ErrorResponse, "description": "인증 실패"},
        404: {"model": ErrorResponse, "description": "키를 찾을 수 없음"}
    }
)
async def get_key_by_name(key_name: str, username: str = Depends(verify_credentials)):
    """이름으로 키 조회"""
    key_store = get_key_store()

    try:
        key_info = key_store.get_key_by_name(key_name)
        if key_info is None:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"키 이름 '{key_name}'을 찾을 수 없습니다"
            )
        return key_info
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"키 조회 중 오류가 발생했습니다: {str(e)}"
        )


@router.put(
    "/keys/{key_id}",
    response_model=KeyResponse,
    summary="키 업데이트",
    description="키의 정보를 업데이트하거나 키를 로테이션합니다",
    responses={
        200: {"description": "키 업데이트 성공"},
        401: {"model": ErrorResponse, "description": "인증 실패"},
        404: {"model": ErrorResponse, "description": "키를 찾을 수 없음"}
    }
)
async def update_key(
    key_id: str,
    request: KeyUpdateRequest,
    username: str = Depends(verify_credentials)
):
    """키 업데이트"""
    key_store = get_key_store()

    try:
        key_info = key_store.update_key(
            key_id=key_id,
            key_name=request.key_name,
            description=request.description,
            rotate=request.rotate
        )

        if key_info is None:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"키 ID '{key_id}'를 찾을 수 없습니다"
            )

        return key_info
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"키 업데이트 중 오류가 발생했습니다: {str(e)}"
        )


@router.delete(
    "/keys/{key_id}",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="키 삭제",
    description="특정 키를 삭제합니다",
    responses={
        204: {"description": "키 삭제 성공"},
        401: {"model": ErrorResponse, "description": "인증 실패"},
        404: {"model": ErrorResponse, "description": "키를 찾을 수 없음"}
    }
)
async def delete_key(key_id: str, username: str = Depends(verify_credentials)):
    """키 삭제"""
    key_store = get_key_store()

    try:
        deleted = key_store.delete_key(key_id)
        if not deleted:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"키 ID '{key_id}'를 찾을 수 없습니다"
            )
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"키 삭제 중 오류가 발생했습니다: {str(e)}"
        )
