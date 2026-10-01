from datetime import datetime
from uuid import uuid4
from sqlalchemy import DateTime,String,Text
from sqlalchemy.orm import Mapped,mapped_column
from .db import Base
class User(Base):
 __tablename__="users"
 id:Mapped[str]=mapped_column(String(36),primary_key=True,default=lambda:str(uuid4()))
 email:Mapped[str]=mapped_column(String(320),unique=True,index=True)
 password_hash:Mapped[str]=mapped_column(Text)
 date_of_birth:Mapped[str]=mapped_column(String(10))
 terms_version:Mapped[str]=mapped_column(String(64))
 privacy_version:Mapped[str]=mapped_column(String(64))
 created_at:Mapped[datetime]=mapped_column(DateTime,default=datetime.utcnow)
