from email.message import EmailMessage

import aiosmtplib

from app.config import Settings, get_settings


async def send_otp_email(
    to: str,
    otp: str,
    expiry_minutes: int,
    settings: Settings | None = None,
) -> None:
    settings = settings or get_settings()

    message = EmailMessage()
    message["From"] = settings.SMTP_FROM
    message["To"] = to
    message["Subject"] = f"Your {settings.APP_NAME} verification code"
    message.set_content(
        f"Your verification code is {otp}.\n\n"
        f"This code expires in {expiry_minutes} minutes.\n\n"
        f"If you did not request this code, you can safely ignore this email."
    )

    await aiosmtplib.send(
        message,
        hostname=settings.SMTP_HOST,
        port=settings.SMTP_PORT,
        username=settings.SMTP_USER,
        password=settings.SMTP_PASSWORD,
        start_tls=settings.SMTP_USE_TLS,
    )
