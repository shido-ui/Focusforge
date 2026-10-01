from datetime import date,datetime,timedelta,timezone
import jwt
from argon2 import PasswordHasher
from .config import get_settings
ph=PasswordHasher()
def hash_password(p:str)->str:return ph.hash(p)
def verify_password(p:str,h:str)->bool:
 try:return ph.verify(h,p)
 except Exception:return False
def is_18_or_older(dob:date,today:date|None=None)->bool:
 t=today or date.today(); return dob<=date(t.year-18,t.month,t.day)
def create_access_token(sub:str)->str:
 s=get_settings(); exp=datetime.now(timezone.utc)+timedelta(minutes=s.access_token_minutes)
 return jwt.encode({"sub":sub,"exp":exp},s.jwt_secret,algorithm="HS256")
