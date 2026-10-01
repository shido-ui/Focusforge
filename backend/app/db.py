from collections.abc import Generator
from sqlalchemy import create_engine,event
from sqlalchemy.orm import DeclarativeBase,Session,sessionmaker
from .config import get_settings
s=get_settings()
engine=create_engine(s.database_url,connect_args={"check_same_thread":False} if s.database_url.startswith("sqlite") else {})
if s.database_url.startswith("sqlite"):
 @event.listens_for(engine,"connect")
 def sqlite_pragmas(conn,_):
  cur=conn.cursor(); cur.execute("PRAGMA journal_mode=WAL"); cur.execute("PRAGMA foreign_keys=ON"); cur.close()
class Base(DeclarativeBase): pass
SessionLocal=sessionmaker(bind=engine,autoflush=False,autocommit=False,expire_on_commit=False)
def get_db()->Generator[Session,None,None]:
 db=SessionLocal()
 try: yield db
 finally: db.close()
