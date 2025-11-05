"""데이터베이스 및 키 저장소"""

import sqlite3
import os
import uuid
from datetime import datetime
from typing import Optional, List
from contextlib import contextmanager
from app.crypto import (
    generate_symmetric_key,
    key_to_base64,
    base64_to_key,
    derive_encryption_key,
    encrypt_key,
    decrypt_key
)


class KeyStore:
    """키 저장소 클래스"""

    def __init__(self, db_path: str, master_password: str):
        """초기화

        Args:
            db_path: 데이터베이스 파일 경로
            master_password: 마스터 비밀번호 (키 암호화에 사용)
        """
        self.db_path = db_path
        self.master_password = master_password
        self._init_db()

    def _init_db(self):
        """데이터베이스 초기화"""
        with self._get_connection() as conn:
            cursor = conn.cursor()

            # 솔트 테이블 (마스터 키 유도용)
            cursor.execute('''
                CREATE TABLE IF NOT EXISTS master_salt (
                    id INTEGER PRIMARY KEY CHECK (id = 1),
                    salt BLOB NOT NULL
                )
            ''')

            # 키 테이블
            cursor.execute('''
                CREATE TABLE IF NOT EXISTS keys (
                    key_id TEXT PRIMARY KEY,
                    key_name TEXT NOT NULL UNIQUE,
                    encrypted_key BLOB NOT NULL,
                    iv BLOB NOT NULL,
                    tag BLOB NOT NULL,
                    key_size INTEGER NOT NULL,
                    description TEXT,
                    created_at TEXT NOT NULL,
                    updated_at TEXT NOT NULL
                )
            ''')

            # 솔트가 없으면 생성
            cursor.execute('SELECT salt FROM master_salt WHERE id = 1')
            if cursor.fetchone() is None:
                salt = os.urandom(32)
                cursor.execute('INSERT INTO master_salt (id, salt) VALUES (1, ?)', (salt,))

            conn.commit()

    @contextmanager
    def _get_connection(self):
        """데이터베이스 연결 컨텍스트 매니저"""
        conn = sqlite3.connect(self.db_path)
        try:
            yield conn
        finally:
            conn.close()

    def _get_master_key(self) -> bytes:
        """마스터 키 가져오기"""
        with self._get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute('SELECT salt FROM master_salt WHERE id = 1')
            salt = cursor.fetchone()[0]
            return derive_encryption_key(self.master_password, salt)

    def create_key(
        self,
        key_name: str,
        key_size: int = 256,
        description: Optional[str] = None
    ) -> dict:
        """새로운 키 생성

        Args:
            key_name: 키 이름
            key_size: 키 크기 (비트)
            description: 키 설명

        Returns:
            생성된 키 정보
        """
        key_id = str(uuid.uuid4())
        key_data = generate_symmetric_key(key_size)

        # 마스터 키로 암호화
        master_key = self._get_master_key()
        encrypted_key, iv, tag = encrypt_key(key_data, master_key)

        now = datetime.utcnow().isoformat()

        with self._get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute('''
                INSERT INTO keys (key_id, key_name, encrypted_key, iv, tag, key_size, description, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            ''', (key_id, key_name, encrypted_key, iv, tag, key_size, description, now, now))
            conn.commit()

        return {
            'key_id': key_id,
            'key_name': key_name,
            'key_value': key_to_base64(key_data),
            'key_size': key_size,
            'description': description,
            'created_at': now,
            'updated_at': now
        }

    def get_key(self, key_id: str) -> Optional[dict]:
        """키 조회

        Args:
            key_id: 키 ID

        Returns:
            키 정보 또는 None
        """
        with self._get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute('''
                SELECT key_id, key_name, encrypted_key, iv, tag, key_size, description, created_at, updated_at
                FROM keys WHERE key_id = ?
            ''', (key_id,))

            row = cursor.fetchone()
            if row is None:
                return None

            key_id, key_name, encrypted_key, iv, tag, key_size, description, created_at, updated_at = row

            # 복호화
            master_key = self._get_master_key()
            key_data = decrypt_key(encrypted_key, master_key, iv, tag)

            return {
                'key_id': key_id,
                'key_name': key_name,
                'key_value': key_to_base64(key_data),
                'key_size': key_size,
                'description': description,
                'created_at': created_at,
                'updated_at': updated_at
            }

    def get_key_by_name(self, key_name: str) -> Optional[dict]:
        """이름으로 키 조회

        Args:
            key_name: 키 이름

        Returns:
            키 정보 또는 None
        """
        with self._get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute('''
                SELECT key_id, key_name, encrypted_key, iv, tag, key_size, description, created_at, updated_at
                FROM keys WHERE key_name = ?
            ''', (key_name,))

            row = cursor.fetchone()
            if row is None:
                return None

            key_id, key_name, encrypted_key, iv, tag, key_size, description, created_at, updated_at = row

            # 복호화
            master_key = self._get_master_key()
            key_data = decrypt_key(encrypted_key, master_key, iv, tag)

            return {
                'key_id': key_id,
                'key_name': key_name,
                'key_value': key_to_base64(key_data),
                'key_size': key_size,
                'description': description,
                'created_at': created_at,
                'updated_at': updated_at
            }

    def list_keys(self) -> List[dict]:
        """모든 키 목록 조회 (키 값 제외)

        Returns:
            키 목록
        """
        with self._get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute('''
                SELECT key_id, key_name, key_size, description, created_at, updated_at
                FROM keys ORDER BY created_at DESC
            ''')

            keys = []
            for row in cursor.fetchall():
                key_id, key_name, key_size, description, created_at, updated_at = row
                keys.append({
                    'key_id': key_id,
                    'key_name': key_name,
                    'key_size': key_size,
                    'description': description,
                    'created_at': created_at,
                    'updated_at': updated_at
                })

            return keys

    def update_key(
        self,
        key_id: str,
        key_name: Optional[str] = None,
        description: Optional[str] = None,
        rotate: bool = False
    ) -> Optional[dict]:
        """키 업데이트

        Args:
            key_id: 키 ID
            key_name: 새로운 키 이름
            description: 새로운 키 설명
            rotate: 키 로테이션 여부

        Returns:
            업데이트된 키 정보 또는 None
        """
        with self._get_connection() as conn:
            cursor = conn.cursor()

            # 기존 키 조회
            cursor.execute('''
                SELECT key_id, key_name, encrypted_key, iv, tag, key_size, description
                FROM keys WHERE key_id = ?
            ''', (key_id,))

            row = cursor.fetchone()
            if row is None:
                return None

            old_key_id, old_key_name, old_encrypted_key, old_iv, old_tag, key_size, old_description = row

            # 업데이트할 값 설정
            new_key_name = key_name if key_name is not None else old_key_name
            new_description = description if description is not None else old_description

            # 키 로테이션
            if rotate:
                key_data = generate_symmetric_key(key_size)
                master_key = self._get_master_key()
                encrypted_key, iv, tag = encrypt_key(key_data, master_key)
            else:
                encrypted_key = old_encrypted_key
                iv = old_iv
                tag = old_tag
                master_key = self._get_master_key()
                key_data = decrypt_key(encrypted_key, master_key, iv, tag)

            now = datetime.utcnow().isoformat()

            cursor.execute('''
                UPDATE keys
                SET key_name = ?, encrypted_key = ?, iv = ?, tag = ?, description = ?, updated_at = ?
                WHERE key_id = ?
            ''', (new_key_name, encrypted_key, iv, tag, new_description, now, key_id))

            conn.commit()

            return {
                'key_id': key_id,
                'key_name': new_key_name,
                'key_value': key_to_base64(key_data),
                'key_size': key_size,
                'description': new_description,
                'created_at': row[0] if len(row) > 7 else now,  # created_at은 변경 안 됨
                'updated_at': now
            }

    def delete_key(self, key_id: str) -> bool:
        """키 삭제

        Args:
            key_id: 키 ID

        Returns:
            삭제 성공 여부
        """
        with self._get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute('DELETE FROM keys WHERE key_id = ?', (key_id,))
            conn.commit()
            return cursor.rowcount > 0
