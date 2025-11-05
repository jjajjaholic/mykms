"""암호화 관련 기능"""

import os
import base64
from cryptography.hazmat.primitives.ciphers import Cipher, algorithms, modes
from cryptography.hazmat.backends import default_backend
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.kdf.pbkdf2 import PBKDF2


def generate_symmetric_key(key_size: int = 256) -> bytes:
    """대칭키 생성

    Args:
        key_size: 키 크기 (비트)

    Returns:
        생성된 대칭키 (bytes)
    """
    key_bytes = key_size // 8
    return os.urandom(key_bytes)


def key_to_base64(key: bytes) -> str:
    """키를 Base64로 인코딩

    Args:
        key: 바이트 형태의 키

    Returns:
        Base64 인코딩된 키 문자열
    """
    return base64.b64encode(key).decode('utf-8')


def base64_to_key(key_b64: str) -> bytes:
    """Base64 문자열을 키로 디코딩

    Args:
        key_b64: Base64 인코딩된 키 문자열

    Returns:
        바이트 형태의 키
    """
    return base64.b64decode(key_b64)


def derive_encryption_key(password: str, salt: bytes) -> bytes:
    """비밀번호로부터 암호화 키 유도

    Args:
        password: 비밀번호
        salt: 솔트

    Returns:
        유도된 암호화 키
    """
    kdf = PBKDF2(
        algorithm=hashes.SHA256(),
        length=32,
        salt=salt,
        iterations=100000,
        backend=default_backend()
    )
    return kdf.derive(password.encode())


def encrypt_key(key_data: bytes, master_key: bytes) -> tuple[bytes, bytes, bytes]:
    """키 데이터를 마스터 키로 암호화

    Args:
        key_data: 암호화할 키 데이터
        master_key: 마스터 키

    Returns:
        (암호화된 데이터, IV, 태그)
    """
    iv = os.urandom(12)  # GCM mode IV
    cipher = Cipher(
        algorithms.AES(master_key),
        modes.GCM(iv),
        backend=default_backend()
    )
    encryptor = cipher.encryptor()
    ciphertext = encryptor.update(key_data) + encryptor.finalize()

    return ciphertext, iv, encryptor.tag


def decrypt_key(ciphertext: bytes, master_key: bytes, iv: bytes, tag: bytes) -> bytes:
    """암호화된 키 데이터를 복호화

    Args:
        ciphertext: 암호화된 데이터
        master_key: 마스터 키
        iv: IV
        tag: 인증 태그

    Returns:
        복호화된 키 데이터
    """
    cipher = Cipher(
        algorithms.AES(master_key),
        modes.GCM(iv, tag),
        backend=default_backend()
    )
    decryptor = cipher.decryptor()
    return decryptor.update(ciphertext) + decryptor.finalize()
