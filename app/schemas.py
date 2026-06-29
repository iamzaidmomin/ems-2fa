from pydantic import BaseModel, EmailStr, Field


class SendOTPRequest(BaseModel):
    email: EmailStr


class SendOTPResponse(BaseModel):
    message: str
    expires_in: int


class VerifyOTPRequest(BaseModel):
    email: EmailStr
    otp: str = Field(min_length=4, max_length=10)


class VerifyOTPResponse(BaseModel):
    verified: bool
    email: str


class HealthResponse(BaseModel):
    status: str
