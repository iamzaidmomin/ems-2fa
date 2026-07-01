from functools import lru_cache

from pydantic import field_validator
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8")

    APP_NAME: str = "twofa"
    DEBUG: bool = True

    DATABASE_URL: str = "sqlite:///./twofa.db"

    OTP_LENGTH: int = 6
    OTP_EXPIRY_MINUTES: int = 10
    OTP_MAX_ATTEMPTS: int = 5
    OTP_SEND_COOLDOWN_SECONDS: int = 60
    OTP_PEPPER: str

    SMTP_HOST: str
    SMTP_PORT: int = 587
    SMTP_USER: str
    SMTP_PASSWORD: str
    SMTP_FROM: str
    SMTP_USE_TLS: bool = True

    @field_validator("OTP_PEPPER")
    @classmethod
    def validate_otp_pepper(cls, value: str) -> str:
        if not value or value == "change-me-to-a-random-secret":
            raise ValueError("OTP_PEPPER must be set to a random secret")
        return value

    @field_validator("SMTP_HOST", "SMTP_USER", "SMTP_PASSWORD", "SMTP_FROM")
    @classmethod
    def validate_smtp_not_placeholder(cls, value: str) -> str:
        placeholders = {"your-email@gmail.com", "your-app-password"}
        if value in placeholders:
            raise ValueError(
                "SMTP settings must be configured in .env (replace placeholder values)"
            )
        return value


@lru_cache
def get_settings() -> Settings:
    settings = Settings()
        # Try to load SMTP secrets from OpenBao
    try:
        bao = OpenBaoService()
        smtp = bao.get_smtp_config()

        settings.SMTP_HOST = smtp["SMTP_HOST"]
        settings.SMTP_PORT = smtp["SMTP_PORT"]
        settings.SMTP_USER = smtp["SMTP_USER"]
        settings.SMTP_PASSWORD = smtp["SMTP_PASSWORD"]
        settings.SMTP_FROM = smtp["SMTP_FROM"]

    except Exception:
        # Fall back to .env if OpenBao is unavailable
        pass

    return settings
