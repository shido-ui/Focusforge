from datetime import date
from app.security import is_18_or_older
def test_adult_boundary():
 assert is_18_or_older(date(2008,10,1),date(2026,10,1))
 assert not is_18_or_older(date(2008,10,2),date(2026,10,1))
