from datetime import date,datetime,timedelta,timezone
import jwt
from argon2 import PasswordHasher
from fastapi import Depends,HTTPException,status
from fastapi.security import HTTPAuthorizationCredentials,HTTPBearer
from .config import get_settings
ph=PasswordHasher()
bearer=HTTPBearer(auto_error=False)
def hash_password(p:str)->str:return ph.hash(p)
def verify_password(p:str,h:str)->bool:
 try:return ph.verify(h,p)
 except Exception:return False
def is_18_or_older(dob:date,today:date|None=None)->bool:
 t=today or date.today(); return dob<=date(t.year-18,t.month,t.day)
def create_access_token(sub:str)->str:
 s=get_settings(); exp=datetime.now(timezone.utc)+timedelta(minutes=s.access_token_minutes)
 return jwt.encode({"sub":sub,"exp":exp},s.jwt_secret,algorithm="HS256")
def get_current_user_id(credentials:HTTPAuthorizationCredentials|None=Depends(bearer))->str:
 if credentials is None or credentials.scheme.lower()!="bearer": raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED,detail="Authentication required.")
 try:
  payload=jwt.decode(credentials.credentials,get_settings().jwt_secret,algorithms=["HS256"])
  subject=payload.get("sub")
  if not isinstance(subject,str) or not subject: raise ValueError
  return subject
 except (jwt.PyJWTError,ValueError): raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED,detail="Invalid or expired session.")
