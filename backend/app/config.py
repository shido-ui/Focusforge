from functools import lru_cache

from pydantic import field_validator, model_validator
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    environment: str = "development"
    database_url: str = "sqlite:///./focusforge.db"
    jwt_secret: str
    access_token_minutes: int = 15
    refresh_token_days: int = 30
    cors_origins: str = "http://localhost,http://localhost:3000,http://127.0.0.1,http://127.0.0.1:3000"
    max_request_bytes: int = 1_048_576
    max_upload_bytes: int = 52_428_800
    login_rate_limit_per_minute: int = 10
    signup_rate_limit_per_minute: int = 5
    refresh_rate_limit_per_minute: int = 20
    account_delete_rate_limit_per_minute: int = 3
    backup_directory: str = "./backups"
    production_api_base_url: str = ""

    model_config = SettingsConfigDict(
        env_file=".env",
        env_prefix="FOCUSFORGE_",
        extra="ignore",
    )

    @field_validator("environment")
    @classmethod
    def normalize_environment(cls, value: str) -> str:
        return value.strip().lower()

    @field_validator("cors_origins")
    @classmethod
    def normalize_origins(cls, value: str) -> str:
        return ",".join(x.strip() for x in value.split(",") if x.strip())

    @model_validator(mode="after")
    def validate_runtime_security(self):
        if not 5 <= self.access_token_minutes <= 60:
            raise ValueError("FOCUSFORGE_ACCESS_TOKEN_MINUTES must be between 5 and 60.")
        if not 1 <= self.refresh_token_days <= 90:
            raise ValueError("FOCUSFORGE_REFRESH_TOKEN_DAYS must be between 1 and 90.")
        if self.max_request_bytes < 16_384:
            raise ValueError("FOCUSFORGE_MAX_REQUEST_BYTES is too small.")
        if self.max_upload_bytes < self.max_request_bytes:
            raise ValueError("FOCUSFORGE_MAX_UPLOAD_BYTES must be >= max request bytes.")
        if self.environment == "production":
            if len(self.jwt_secret) < 32:
                raise ValueError("FOCUSFORGE_JWT_SECRET must be at least 32 characters in production.")
            origins = self.allowed_origins
            if not origins:
                raise ValueError("FOCUSFORGE_CORS_ORIGINS is required in production.")
            if any(not origin.startswith("https://") for origin in origins):
                raise ValueError("Production CORS origins must use HTTPS.")
            if any(origin == "*" for origin in origins):
                raise ValueError("Wildcard CORS is forbidden in production.")
            if not self.production_api_base_url.startswith("https://"):
                raise ValueError("FOCUSFORGE_PRODUCTION_API_BASE_URL must use HTTPS in production.")
        return self

    @property
    def allowed_origins(self) -> list[str]:
        return [x for x in self.cors_origins.split(",") if x]


@lru_cache
def get_settings() -> Settings:
    return Settings()
