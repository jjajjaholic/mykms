"""Pydantic models for request/response validation"""

from pydantic import BaseModel, Field
from typing import Optional
from datetime import datetime


class KeyCreateRequest(BaseModel):
    """키 생성 요청"""
    key_name: str = Field(..., description="키 이름", min_length=1, max_length=255)
    key_size: int = Field(default=256, description="키 크기 (비트)", ge=128, le=512)
    description: Optional[str] = Field(None, description="키 설명", max_length=1000)

    class Config:
        json_schema_extra = {
            "example": {
                "key_name": "my-encryption-key",
                "key_size": 256,
                "description": "데이터 암호화를 위한 대칭키"
            }
        }


class KeyUpdateRequest(BaseModel):
    """키 업데이트 요청"""
    key_name: Optional[str] = Field(None, description="새로운 키 이름", min_length=1, max_length=255)
    description: Optional[str] = Field(None, description="새로운 키 설명", max_length=1000)
    rotate: bool = Field(default=False, description="키 로테이션 (새로운 키 값 생성)")

    class Config:
        json_schema_extra = {
            "example": {
                "key_name": "updated-key-name",
                "description": "업데이트된 설명",
                "rotate": False
            }
        }


class KeyResponse(BaseModel):
    """키 응답"""
    key_id: str = Field(..., description="키 ID (UUID)")
    key_name: str = Field(..., description="키 이름")
    key_value: str = Field(..., description="키 값 (Base64 인코딩)")
    key_size: int = Field(..., description="키 크기 (비트)")
    description: Optional[str] = Field(None, description="키 설명")
    created_at: datetime = Field(..., description="생성 시간")
    updated_at: datetime = Field(..., description="수정 시간")

    class Config:
        json_schema_extra = {
            "example": {
                "key_id": "123e4567-e89b-12d3-a456-426614174000",
                "key_name": "my-encryption-key",
                "key_value": "SGVsbG8gV29ybGQ=",
                "key_size": 256,
                "description": "데이터 암호화를 위한 대칭키",
                "created_at": "2025-01-01T00:00:00",
                "updated_at": "2025-01-01T00:00:00"
            }
        }


class KeyListResponse(BaseModel):
    """키 목록 응답"""
    key_id: str = Field(..., description="키 ID (UUID)")
    key_name: str = Field(..., description="키 이름")
    key_size: int = Field(..., description="키 크기 (비트)")
    description: Optional[str] = Field(None, description="키 설명")
    created_at: datetime = Field(..., description="생성 시간")
    updated_at: datetime = Field(..., description="수정 시간")

    class Config:
        json_schema_extra = {
            "example": {
                "key_id": "123e4567-e89b-12d3-a456-426614174000",
                "key_name": "my-encryption-key",
                "key_size": 256,
                "description": "데이터 암호화를 위한 대칭키",
                "created_at": "2025-01-01T00:00:00",
                "updated_at": "2025-01-01T00:00:00"
            }
        }


class ErrorResponse(BaseModel):
    """에러 응답"""
    detail: str = Field(..., description="에러 메시지")

    class Config:
        json_schema_extra = {
            "example": {
                "detail": "인증에 실패했습니다"
            }
        }
