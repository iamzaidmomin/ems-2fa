import hashlib
import secrets
from datetime import UTC, datetime, timedelta

from fastapi import HTTPException, status
from sqlalchemy.orm import Session

from app.config import Settings, get_settings
from app.models import OTPRequest
from app.services.email import send_otp_email


def normalize_email(email: str) -> str:
    return email.strip().lower()


def hash_otp(otp: str, pepper: str) -> str:
    return hashlib.sha256(f"{otp}:{pepper}".encode()).hexdigest()


def generate_otp(length: int) -> str:
    upper = 10**length
    return str(secrets.randbelow(upper)).zfill(length)


def _utcnow() -> datetime:
    return datetime.now(UTC).replace(tzinfo=None)


async def send_otp(db: Session, email: str, settings: Settings | None = None) -> int:
    settings = settings or get_settings()
    normalized = normalize_email(email)
    now = _utcnow()

    existing = (
        db.query(OTPRequest)
        .filter(
            OTPRequest.email == normalized,
            OTPRequest.verified_at.is_(None),
        )
        .order_by(OTPRequest.created_at.desc())
        .first()
    )

    if existing:
        cooldown_end = existing.created_at + timedelta(seconds=settings.OTP_SEND_COOLDOWN_SECONDS)
        if now < cooldown_end:
            retry_after = int((cooldown_end - now).total_seconds())
            raise HTTPException(
                status_code=status.HTTP_429_TOO_MANY_REQUESTS,
                detail=f"Please wait {retry_after} seconds before requesting another OTP",
            )

    otp = generate_otp(settings.OTP_LENGTH)
    expires_at = now + timedelta(minutes=settings.OTP_EXPIRY_MINUTES)

    db.query(OTPRequest).filter(
        OTPRequest.email == normalized,
        OTPRequest.verified_at.is_(None),
    ).delete()

    record = OTPRequest(
        email=normalized,
        otp_hash=hash_otp(otp, settings.OTP_PEPPER),
        created_at=now,
        expires_at=expires_at,
        attempts=0,
    )
    db.add(record)
    db.commit()

    try:
        await send_otp_email(
            to=normalized,
            otp=otp,
            expiry_minutes=settings.OTP_EXPIRY_MINUTES,
            settings=settings,
        )
    except Exception as exc:
        db.delete(record)
        db.commit()
        raise HTTPException(
            status_code=status.HTTP_502_BAD_GATEWAY,
            detail="Failed to send OTP email",
        ) from exc

    return settings.OTP_EXPIRY_MINUTES * 60


def verify_otp(db: Session, email: str, otp: str, settings: Settings | None = None) -> str:
    settings = settings or get_settings()
    normalized = normalize_email(email)
    now = _utcnow()

    record = (
        db.query(OTPRequest)
        .filter(
            OTPRequest.email == normalized,
            OTPRequest.verified_at.is_(None),
        )
        .order_by(OTPRequest.created_at.desc())
        .first()
    )

    if not record:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="No pending OTP found for this email",
        )

    if now > record.expires_at:
        raise HTTPException(
            status_code=status.HTTP_410_GONE,
            detail="OTP has expired",
        )

    if record.attempts >= settings.OTP_MAX_ATTEMPTS:
        raise HTTPException(
            status_code=status.HTTP_429_TOO_MANY_REQUESTS,
            detail="Maximum verification attempts exceeded",
        )

    if hash_otp(otp, settings.OTP_PEPPER) != record.otp_hash:
        record.attempts += 1
        db.commit()
        remaining = settings.OTP_MAX_ATTEMPTS - record.attempts
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Invalid OTP. {remaining} attempt(s) remaining",
        )

    record.verified_at = now
    db.commit()
    return normalized
