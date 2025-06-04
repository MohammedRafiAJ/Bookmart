package com.bookStore.service;

import com.bookStore.base.Review;
import com.bookStore.config.ApiConstants;
import com.bookStore.utils.RestUtil;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;

public class ReviewService {
    public static Response postReview(int bookId, Review review, String token) {
        Map<String, Object> form = new HashMap<>();
        form.put("rating", review.getRating());
        form.put("review_text", review.getReviewText());
        form.put("user_email", review.getUserEmail());
        return RestUtil.post(ApiConstants.BOOKS + bookId + "/reviews/", form, token);
    }

    public static Response getReviews(int bookId, String token) {
        return RestUtil.get(ApiConstants.BOOKS + bookId + "/reviews/", token);
    }

    public static Response getAverageRating(int bookId, String token) {
        return RestUtil.get(ApiConstants.BOOKS + bookId + "/rating/", token);
    }
} 