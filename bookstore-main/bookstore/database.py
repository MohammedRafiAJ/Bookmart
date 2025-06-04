from typing import Optional, List
from datetime import datetime
from pydantic import BaseModel

from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
from sqlmodel import Field, SQLModel, Relationship

DATABASE_URL = "sqlite:///./test.db"  # Example using SQLite
Base = declarative_base()


class UserCredentials(SQLModel, table=True):
    __tablename__ = "user_credentials"
    id: Optional[int] = Field(default=None, primary_key=True, index=True)
    email: str = Field(index=True, unique=True)
    password: str

    def dict(self):
        return {
            "id": self.id,
            "email": self.email
        }


class Book(SQLModel, table=True):
    __tablename__ = "books"
    id: Optional[int] = Field(default=None, primary_key=True, index=True)
    name: str = Field(index=True)
    author: str = Field(index=True)
    published_year: int
    book_summary: str
    cover_image_url: Optional[str] = None
    tags: Optional[str] = None  # Comma-separated tags

    def dict(self):
        return {
            "id": self.id,
            "name": self.name,
            "author": self.author,
            "published_year": self.published_year,
            "book_summary": self.book_summary,
            "cover_image_url": self.cover_image_url,
            "tags": self.tags.split(",") if self.tags else []
        }


class Review(SQLModel, table=True):
    __tablename__ = "reviews"
    id: Optional[int] = Field(default=None, primary_key=True, index=True)
    book_id: int = Field(foreign_key="books.id")
    user_email: str
    rating: int
    review_text: str
    created_at: datetime = Field(default_factory=datetime.utcnow)

    def dict(self):
        return {
            "id": self.id,
            "book_id": self.book_id,
            "user_email": self.user_email,
            "rating": self.rating,
            "review_text": self.review_text,
            "created_at": self.created_at.isoformat()
        }


class AuditLog(SQLModel, table=True):
    __tablename__ = "audit_logs"
    id: Optional[int] = Field(default=None, primary_key=True, index=True)
    book_id: int = Field(foreign_key="books.id")
    action: str
    user_email: str
    timestamp: datetime = Field(default_factory=datetime.utcnow)
    details: str

    def dict(self):
        return {
            "id": self.id,
            "book_id": self.book_id,
            "action": self.action,
            "user_email": self.user_email,
            "timestamp": self.timestamp.isoformat(),
            "details": self.details
        }


class BorrowRecord(SQLModel, table=True):
    __tablename__ = "borrow_records"
    id: Optional[int] = Field(default=None, primary_key=True, index=True)
    book_id: int = Field(foreign_key="books.id")
    user_email: str
    borrowed_at: datetime = Field(default_factory=datetime.utcnow)
    due_date: datetime
    returned_at: Optional[datetime] = None

    def dict(self):
        return {
            "id": self.id,
            "book_id": self.book_id,
            "user_email": self.user_email,
            "borrowed_at": self.borrowed_at.isoformat(),
            "due_date": self.due_date.isoformat(),
            "returned_at": self.returned_at.isoformat() if self.returned_at else None
        }


class Notification(SQLModel, table=True):
    __tablename__ = "notifications"
    id: Optional[int] = Field(default=None, primary_key=True, index=True)
    user_email: str
    message: str
    created_at: datetime = Field(default_factory=datetime.utcnow)
    read: bool = False

    def dict(self):
        return {
            "id": self.id,
            "user_email": self.user_email,
            "message": self.message,
            "created_at": self.created_at.isoformat(),
            "read": self.read
        }


engine = create_engine(DATABASE_URL)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

SQLModel.metadata.create_all(engine)


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
