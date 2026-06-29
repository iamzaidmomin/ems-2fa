from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.database import get_db
from app.schemas import SendOTPRequest, SendOTPResponse, VerifyOTPRequest, VerifyOTPResponse
from app.services import otp as otp_service

router = APIRouter()


@router.post("/send", response_model=SendOTPResponse)
async def send_otp(payload: SendOTPRequest, db: Session = Depends(get_db)) -> SendOTPResponse:
    expires_in = await otp_service.send_otp(db, payload.email)
    return SendOTPResponse(message="OTP sent", expires_in=expires_in)


@router.post("/verify", response_model=VerifyOTPResponse)
def verify_otp(payload: VerifyOTPRequest, db: Session = Depends(get_db)) -> VerifyOTPResponse:
    email = otp_service.verify_otp(db, payload.email, payload.otp)
    return VerifyOTPResponse(verified=True, email=email)
