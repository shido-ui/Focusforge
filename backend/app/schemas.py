from datetime import date
from pydantic import BaseModel,EmailStr,Field
class SignupRequest(BaseModel):
 email:EmailStr
 password:str=Field(min_length=12,max_length=128)
 date_of_birth:date
 terms_version:str=Field(min_length=1,max_length=64)
 privacy_version:str=Field(min_length=1,max_length=64)
class LoginRequest(BaseModel):
 email:EmailStr
 password:str=Field(min_length=1,max_length=128)
class TokenResponse(BaseModel):
 access_token:str
 token_type:str="bearer"
