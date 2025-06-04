# Write a FastAPI entrypoint

from datetime import timedelta

from fastapi import Depends, FastAPI
from sqlalchemy.orm import Session
from sqlalchemy import and_

from bookmgmt import router as book_router
from database import UserCredentials, get_db, BorrowRecord, Notification
from utils import create_access_token
from threading import Timer
from datetime import datetime

app = FastAPI()

app.include_router(book_router, tags=["Books"])


@app.get("/health")
async def get_health():
    return {"status": "up"}


from fastapi import HTTPException
from passlib.context import CryptContext

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")


@app.post("/signup")
async def create_user_signup(user_credentials: UserCredentials, db: Session = Depends(get_db)):
    user = db.query(UserCredentials).filter(UserCredentials.email == user_credentials.email).first()
    if user:
        raise HTTPException(status_code=400, detail="Email already registered")
    hashed_password = pwd_context.hash(user_credentials.password)
    user_credentials.password = hashed_password
    db.add(user_credentials)
    db.commit()
    db.refresh(user_credentials)
    return {"message": "User created successfully"}


@app.post("/login")
async def login_for_access_token(user_credentials: UserCredentials, db: Session = Depends(get_db)):
    user = db.query(UserCredentials).filter(UserCredentials.email == user_credentials.email).first()
    if not user or not pwd_context.verify(user_credentials.password, user.password):
        raise HTTPException(status_code=400, detail="Incorrect email or password")
    access_token_expires = timedelta(minutes=30)
    access_token = create_access_token(data={"sub": user.email}, expires_delta=access_token_expires)
    return {"access_token": access_token, "token_type": "bearer"}

def check_due_books():
    db = next(get_db())
    now = datetime.utcnow()
    soon = now + timedelta(days=1)
    due_soon = db.query(BorrowRecord).filter(and_(BorrowRecord.due_date <= soon, BorrowRecord.returned_at.is_(None))).all()
    for borrow in due_soon:
        # Check if notification already exists
        exists = db.query(Notification).filter(and_(Notification.user_email == borrow.user_email, Notification.message.contains(str(borrow.book_id)), Notification.read == False)).first()
        if not exists:
            notif = Notification(user_email=borrow.user_email, message=f"Book {borrow.book_id} is due soon!", created_at=now)
            db.add(notif)
    db.commit()
    Timer(3600, check_due_books).start()  # Check every hour

@app.on_event("startup")
def startup_event():
    Timer(5, check_due_books).start()  # Start after 5 seconds
