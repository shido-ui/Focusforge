from functools import lru_cache
from pydantic_settings import BaseSettings, SettingsConfigDict
class Settings(BaseSettings):
    environment:str="development"
    database_url:str="sqlite:///./focusforge.db"
    jwt_secret:str
    access_token_minutes:int=15
    model_config=SettingsConfigDict(env_file=".env",env_prefix="FOCUSFORGE_",extra="ignore")
@lru_cache
def get_settings()->Settings: return Settings()
