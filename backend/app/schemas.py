from datetime import date, datetime

from pydantic import BaseModel, EmailStr, Field, field_validator


class SignupRequest(BaseModel):
    email: EmailStr
    password: str = Field(min_length=12, max_length=128)
    date_of_birth: date
    terms_version: str = Field(min_length=1, max_length=64)
    privacy_version: str = Field(min_length=1, max_length=64)

    @field_validator("email")
    @classmethod
    def normalize_email(cls, value: EmailStr) -> EmailStr:
        text = str(value)
        if text != text.strip():
            raise ValueError("Email must not contain leading or trailing whitespace.")
        return EmailStr(text.lower())

    @field_validator("date_of_birth")
    @classmethod
    def validate_date_of_birth(cls, value: date) -> date:
        if value > date.today():
            raise ValueError("Date of birth cannot be in the future.")
        return value


class LoginRequest(BaseModel):
    email: EmailStr
    password: str = Field(min_length=1, max_length=128)

    @field_validator("email")
    @classmethod
    def normalize_email(cls, value: EmailStr) -> EmailStr:
        text = str(value)
        if text != text.strip():
            raise ValueError("Email must not contain leading or trailing whitespace.")
        return EmailStr(text.lower())


class RefreshRequest(BaseModel):
    refresh_token: str = Field(min_length=32, max_length=512)
    device_label: str | None = Field(default=None, max_length=128)


class TokenResponse(BaseModel):
    access_token: str
    refresh_token: str
    token_type: str = "bearer"
    access_token_expires_at: datetime


class AccountResponse(BaseModel):
    id: str
    email: EmailStr


class SyncEventRequest(BaseModel):
    client_event_id: str = Field(min_length=16, max_length=64, pattern=r"^[A-Za-z0-9_-]+$")
    event_type: str = Field(min_length=1, max_length=64, pattern=r"^[A-Za-z0-9_.:-]+$")
    payload: dict = Field(default_factory=dict)
    client_created_at: datetime


class SyncEventResponse(BaseModel):
    client_event_id: str
    accepted: bool
    duplicate: bool
