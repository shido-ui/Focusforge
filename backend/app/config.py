from functools import lru_cache

from pydantic import model_validator
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    environment: str = "development"
    database_url: str = "sqlite:///./focusforge.db"
    jwt_secret: str
    access_token_minutes: int = 15

    model_config = SettingsConfigDict(
        env_file=".env",
        env_prefix="FOCUSFORGE_",
        extra="ignore",
    )

    @model_validator(mode="after")
    def validate_runtime_security(self):
        if self.access_token_minutes < 5 or self.access_token_minutes > 60:
            raise ValueError("FOCUSFORGE_ACCESS_TOKEN_MINUTES must be between 5 and 60.")
        if self.environment.lower() == "production" and len(self.jwt_secret) < 32:
            raise ValueError("FOCUSFORGE_JWT_SECRET must be at least 32 characters in production.")
        return self


@lru_cache
def get_settings() -> Settings:
    return Settings()
