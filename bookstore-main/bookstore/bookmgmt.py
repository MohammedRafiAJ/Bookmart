from typing import List, Optional

from fastapi import APIRouter, Depends, HTTPException, UploadFile, File, Form, status
from sqlalchemy.orm import Session
from fastapi.responses import FileResponse, JSONResponse
import os
from datetime import datetime, timedelta
from sqlalchemy import func, and_, or_, text, String, cast, desc, null, not_
from sqlmodel import select, col

from database import Book, Review, AuditLog, BorrowRecord, Notification, UserCredentials  # Assuming these are defined in your models.py
from database import get_db
from middleware import JWTBearer

router = APIRouter()

IMAGES_DIR = os.path.join(os.path.dirname(__file__), "images")
os.makedirs(IMAGES_DIR, exist_ok=True)


@router.post("/books/", response_model=Book, dependencies=[Depends(JWTBearer())])
async def create_book(
    name: str = Form(...),
    author: str = Form(...),
    published_year: int = Form(...),
    book_summary: str = Form(...),
    cover_image: Optional[UploadFile] = File(None),
    db: Session = Depends(get_db)
):
    try:
        cover_image_url = None
        if cover_image and cover_image.filename:
            image_path = os.path.join(IMAGES_DIR, cover_image.filename)
            try:
                with open(image_path, "wb") as f:
                    f.write(await cover_image.read())
                cover_image_url = f"/books/images/{cover_image.filename}"
            except Exception as e:
                raise HTTPException(status_code=400, detail=f"Image upload failed: {str(e)}")
        book = Book(
            name=name,
            author=author,
            published_year=published_year,
            book_summary=book_summary,
            cover_image_url=cover_image_url
        )
        db.add(book)
        db.commit()
        db.refresh(book)
        return book
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Book creation failed: {str(e)}")


@router.put("/books/{book_id}", response_model=Book, dependencies=[Depends(JWTBearer())])
async def update_book(
    book_id: int,
    update_data: Book,
    db: Session = Depends(get_db),
    user_email: str = Form("system")
):
    db_book = db.get(Book, book_id)
    if db_book is None:
        raise HTTPException(status_code=404, detail="Book not found")
    update_data_dict = update_data.dict()
    for key, value in update_data_dict.items():
        if value is not None:
            setattr(db_book, key, value)
    db.add(db_book)
    db.commit()
    db.refresh(db_book)
    # Log the update
    log = AuditLog(
        book_id=book_id,
        action="update",
        user_email=user_email,
        details=str(update_data_dict)
    )
    db.add(log)
    db.commit()
    return db_book


@router.delete("/books/{book_id}", dependencies=[Depends(JWTBearer())])
async def delete_book(
    book_id: int,
    db: Session = Depends(get_db),
    user_email: str = Form("system")
):
    db_book = db.get(Book, book_id)
    if db_book is None:
        raise HTTPException(status_code=404, detail="Book not found")
    db.delete(db_book)
    db.commit()
    # Log the delete
    log = AuditLog(
        book_id=book_id,
        action="delete",
        user_email=user_email,
        details="Book deleted"
    )
    db.add(log)
    db.commit()
    return {"message": "Book deleted successfully"}


@router.get("/books/{book_id}", response_model=Book, dependencies=[Depends(JWTBearer())])
async def get_book_by_id(book_id: int, db: Session = Depends(get_db)):
    db_book = db.get(Book, book_id)
    if db_book is None:
        raise HTTPException(status_code=404, detail="Book not found")
    return db_book


@router.get("/books/", response_model=List[Book], dependencies=[Depends(JWTBearer())])
async def get_all_books(
    author: Optional[str] = None,
    year_min: Optional[int] = None,
    year_max: Optional[int] = None,
    q: Optional[str] = None,
    db: Session = Depends(get_db)
):
    query = select(Book)
    if author:
        query = query.where(col(Book.author).ilike(f"%{author}%"))
    if year_min is not None:
        query = query.where(col(Book.published_year) >= year_min)
    if year_max is not None:
        query = query.where(col(Book.published_year) <= year_max)
    if q:
        query = query.where(
            or_(
                col(Book.name).ilike(f"%{q}%"),
                cast(col(Book.book_summary), String).ilike(f"%{q}%")
            )
        )
    books = db.execute(query).scalars().all()
    return JSONResponse(content=[book.dict() for book in books])


@router.get("/books/images/{filename}")
def get_book_image(filename: str):
    image_path = os.path.join(IMAGES_DIR, filename)
    if not os.path.exists(image_path):
        raise HTTPException(status_code=404, detail="Image not found")
    return FileResponse(image_path)


@router.post("/books/{book_id}/reviews/", status_code=status.HTTP_201_CREATED)
async def add_review(
    book_id: int,
    rating: int = Form(...),
    review_text: str = Form(...),
    db: Session = Depends(get_db),
    user_email: str = Form(...)
):
    db_book = db.get(Book, book_id)
    if not db_book:
        raise HTTPException(status_code=404, detail="Book not found")
    review = Review(
        book_id=book_id,
        user_email=user_email,
        rating=rating,
        review_text=review_text,
        created_at=datetime.utcnow()
    )
    db.add(review)
    db.commit()
    db.refresh(review)
    return review


@router.get("/books/{book_id}/reviews/", response_model=List[Review])
async def get_reviews(book_id: int, db: Session = Depends(get_db)):
    try:
        query = select(Review).where(col(Review.book_id) == book_id)
        reviews = db.execute(query).scalars().all()
        return reviews
    except Exception:
        return []


@router.get("/books/{book_id}/rating/")
async def get_average_rating(book_id: int, db: Session = Depends(get_db)):
    query = select(Review.rating).where(col(Review.book_id) == book_id)
    ratings = db.execute(query).scalars().all()
    avg_rating = sum(r for r in ratings) / len(ratings) if ratings else 0
    return JSONResponse(
        content={
            "book_id": book_id,
            "average_rating": round(avg_rating, 2),
            "total_ratings": len(ratings)
        }
    )


@router.get("/books/recommendations/", response_model=List[Book])
async def get_recommendations(user_email: str, db: Session = Depends(get_db)):
    # Get user's borrowing history (only borrowed and returned)
    borrow_query = select(BorrowRecord).where(
        and_(
            col(BorrowRecord.user_email) == user_email,
            not_(col(BorrowRecord.returned_at).is_(null()))
        )
    )
    borrows = db.execute(borrow_query).scalars().all()

    if not borrows:
        # If no borrowing history, return most borrowed books
        popular_query = select(
            col(BorrowRecord.book_id),
            func.count(col(BorrowRecord.id)).label("borrow_count")
        ).group_by(col(BorrowRecord.book_id)).order_by(desc("borrow_count")).limit(5)

        popular_books = db.execute(popular_query).all()
        book_ids = [record[0] for record in popular_books]

        if not book_ids:
            return []

        books_query = select(Book).where(col(Book.id).in_(book_ids))
        return db.execute(books_query).scalars().all()

    # Find similar users who borrowed the same books
    similar_users_query = select(
        col(BorrowRecord.user_email),
        func.count(col(BorrowRecord.id)).label("common_books")
    ).where(
        and_(
            col(BorrowRecord.book_id).in_([b.book_id for b in borrows]),
            col(BorrowRecord.user_email) != user_email
        )
    ).group_by(col(BorrowRecord.user_email)).order_by(desc("common_books")).limit(5)

    similar_users = db.execute(similar_users_query).all()
    if not similar_users:
        return []

    # Recommend books borrowed by similar users that the current user hasn't borrowed
    recommendations_query = select(
        col(BorrowRecord.book_id),
        func.count(col(BorrowRecord.id)).label("borrow_count")
    ).where(
        and_(
            col(BorrowRecord.user_email).in_([u[0] for u in similar_users]),
            not_(col(BorrowRecord.book_id).in_([b.book_id for b in borrows]))
        )
    ).group_by(col(BorrowRecord.book_id)).order_by(desc("borrow_count")).limit(5)

    recommended_books = db.execute(recommendations_query).all()
    if not recommended_books:
        return []

    book_ids = [record[0] for record in recommended_books]
    books_query = select(Book).where(col(Book.id).in_(book_ids))
    return db.execute(books_query).scalars().all()


@router.get("/books/{book_id}/history/", response_model=List[AuditLog])
async def get_book_history(book_id: int, db: Session = Depends(get_db)):
    try:
        query = select(AuditLog).where(col(AuditLog.book_id) == book_id).order_by(col(AuditLog.timestamp).desc())
        logs = db.execute(query).scalars().all()
        return logs
    except Exception:
        return []


@router.post("/books/{book_id}/borrow/")
async def borrow_book(book_id: int, user_email: str = Form(...), db: Session = Depends(get_db)):
    db_book = db.get(Book, book_id)
    if not db_book:
        raise HTTPException(status_code=404, detail="Book not found")
    # Check if already borrowed and not returned
    active_borrow_query = select(BorrowRecord).where(
        and_(
            col(BorrowRecord.book_id) == book_id,
            col(BorrowRecord.returned_at).is_(null())
        )
    )
    active_borrow = db.execute(active_borrow_query).scalar_one_or_none()
    if active_borrow:
        raise HTTPException(status_code=400, detail="Book already borrowed")
    due_date = datetime.utcnow() + timedelta(days=14)
    borrow = BorrowRecord(book_id=book_id, user_email=user_email, due_date=due_date)
    db.add(borrow)
    db.commit()
    db.refresh(borrow)
    return borrow


@router.post("/books/{book_id}/return/")
async def return_book(book_id: int, user_email: str = Form(...), db: Session = Depends(get_db)):
    borrow_query = select(BorrowRecord).where(
        and_(
            col(BorrowRecord.book_id) == book_id,
            col(BorrowRecord.user_email) == user_email,
            col(BorrowRecord.returned_at).is_(null())
        )
    )
    borrow = db.execute(borrow_query).scalar_one_or_none()
    if not borrow:
        raise HTTPException(
            status_code=404,
            detail="No active borrow record found for this user and book"
        )
    borrow.returned_at = datetime.utcnow()
    db.commit()
    db.refresh(borrow)
    return borrow


@router.get("/books/{book_id}/borrowers/", response_model=List[BorrowRecord])
async def get_borrow_history(book_id: int, db: Session = Depends(get_db)):
    query = select(BorrowRecord).where(col(BorrowRecord.book_id) == book_id).order_by(col(BorrowRecord.borrowed_at).desc())
    records = db.execute(query).scalars().all()
    return records


# --- Notifications ---
@router.get("/notifications/", response_model=List[Notification])
async def get_notifications(user_email: str, db: Session = Depends(get_db)):
    query = select(Notification).where(col(Notification.user_email) == user_email).order_by(col(Notification.created_at).desc())
    return db.execute(query).scalars().all()


@router.post("/notifications/mark_read/")
async def mark_notification_read(notification_id: int, db: Session = Depends(get_db)):
    notif = db.get(Notification, notification_id)
    if notif:
        notif.read = True
        db.commit()
    return {"success": True}


# --- Analytics ---
@router.get("/analytics/most_borrowed/")
async def most_borrowed_books(db: Session = Depends(get_db)):
    query = select(
        col(BorrowRecord.book_id),
        func.count(col(BorrowRecord.id)).label("borrow_count")
    ).group_by(col(BorrowRecord.book_id)).order_by(desc("borrow_count")).limit(5)
    results = db.execute(query).all()
    books = [db.get(Book, r[0]) for r in results]
    return books


@router.get("/analytics/top_rated/")
async def top_rated_books(db: Session = Depends(get_db)):
    query = select(
        col(Review.book_id),
        func.avg(col(Review.rating)).label("avg_rating")
    ).group_by(col(Review.book_id)).order_by(desc("avg_rating")).limit(5)
    results = db.execute(query).all()
    books = [db.get(Book, r[0]) for r in results]
    return books


@router.get("/analytics/active_users/")
async def active_users(db: Session = Depends(get_db)):
    query = select(
        col(BorrowRecord.user_email),
        func.count(col(BorrowRecord.id)).label("borrow_count")
    ).group_by(col(BorrowRecord.user_email)).order_by(desc("borrow_count")).limit(5)
    results = db.execute(query).all()
    return [{"user_email": r[0], "borrow_count": r[1]} for r in results]


# --- Tagging ---
@router.post("/books/{book_id}/tags/")
async def add_tags(book_id: int, tags: str = Form(...), db: Session = Depends(get_db)):
    db_book = db.get(Book, book_id)
    if not db_book:
        raise HTTPException(status_code=404, detail="Book not found")
    db_book.tags = tags
    db.commit()
    db.refresh(db_book)
    return db_book


@router.get("/books/by_tag/", response_model=List[Book])
async def get_books_by_tag(tag: str, db: Session = Depends(get_db)):
    query = select(Book).where(
        and_(
            col(Book.tags).is_not(None),
            cast(col(Book.tags), String).ilike(f"%{tag}%")
        )
    )
    return db.execute(query).scalars().all()


# --- User Profile & History ---
@router.get("/users/{email}/profile/")
async def get_user_profile(email: str, db: Session = Depends(get_db)):
    query = select(UserCredentials).where(col(UserCredentials.email) == email)
    user = db.execute(query).scalar_one_or_none()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    return user


@router.put("/users/{email}/profile/")
async def update_user_profile(email: str, password: str = Form(...), db: Session = Depends(get_db)):
    query = select(UserCredentials).where(col(UserCredentials.email) == email)
    user = db.execute(query).scalar_one_or_none()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    user.password = password
    db.commit()
    db.refresh(user)
    return user


@router.get("/users/{email}/history/")
async def get_user_history(email: str, db: Session = Depends(get_db)):
    borrows_query = select(BorrowRecord).where(col(BorrowRecord.user_email) == email)
    reviews_query = select(Review).where(col(Review.user_email) == email)
    borrows = db.execute(borrows_query).scalars().all()
    reviews = db.execute(reviews_query).scalars().all()
    return {"borrows": borrows, "reviews": reviews}