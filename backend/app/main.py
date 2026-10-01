from fastapi import Depends,FastAPI,HTTPException,status
from sqlalchemy import select
from sqlalchemy.orm import Session
from .db import get_db
from .models import User
from .schemas import LoginRequest,SignupRequest,TokenResponse
from .security import create_access_token,hash_password,is_18_or_older,verify_password
app=FastAPI(title="FocusForge API",version="0.1.0")
@app.get("/api/v1/health")
def health(): return {"status":"ok","service":"focusforge-api"}
@app.post("/api/v1/auth/signup",response_model=TokenResponse,status_code=status.HTTP_201_CREATED)
def signup(p:SignupRequest,db:Session=Depends(get_db)):
 if not is_18_or_older(p.date_of_birth): raise HTTPException(403,"FocusForge requires users to be 18 or older.")
 email=p.email.lower()
 if db.scalar(select(User).where(User.email==email)): raise HTTPException(409,"An account with this email already exists.")
 u=User(email=email,password_hash=hash_password(p.password),date_of_birth=p.date_of_birth.isoformat(),terms_version=p.terms_version,privacy_version=p.privacy_version)
 db.add(u); db.commit(); db.refresh(u)
 return TokenResponse(access_token=create_access_token(u.id))
@app.post("/api/v1/auth/login",response_model=TokenResponse)
def login(p:LoginRequest,db:Session=Depends(get_db)):
 u=db.scalar(select(User).where(User.email==p.email.lower()))
 if not u or not verify_password(p.password,u.password_hash): raise HTTPException(401,"Invalid credentials.")
 return TokenResponse(access_token=create_access_token(u.id))
