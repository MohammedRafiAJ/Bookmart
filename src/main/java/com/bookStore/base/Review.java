package com.bookStore.base;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;

public class Review {
    @JsonProperty("id")
    private int id;
    @JsonProperty("book_id")
    private int bookId;
    @JsonProperty("user_email")
    private String userEmail;
    @JsonProperty("rating")
    private int rating;
    @JsonProperty("review_text")
    private String reviewText;
    @JsonProperty("created_at")
    private Date createdAt;

    public Review() {}

    public Review(int bookId, String userEmail, int rating, String reviewText) {
        this.bookId = bookId;
        this.userEmail = userEmail;
        this.rating = rating;
        this.reviewText = reviewText;
    }

    public int getId() { return id; }
    public int getBookId() { return bookId; }
    public String getUserEmail() { return userEmail; }
    public int getRating() { return rating; }
    public String getReviewText() { return reviewText; }
    public Date getCreatedAt() { return createdAt; }
} 