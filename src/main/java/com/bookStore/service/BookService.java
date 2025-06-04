package com.bookStore.service;

import com.bookStore.base.Book;
import com.bookStore.config.ApiConstants;
import com.bookStore.utils.RestUtil;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.io.File;

public class BookService {

    public static Response createBook(Book book, String token, File coverImage) {
        if (coverImage != null) {
            return RestAssured.given()
                    .baseUri(ApiConstants.BASE_URI)
                    .contentType(ContentType.MULTIPART)
                    .auth().oauth2(token)
                    .multiPart("name", book.getName())
                    .multiPart("author", book.getAuthor())
                    .multiPart("published_year", book.getPublishedYear())
                    .multiPart("book_summary", book.getBookSummary())
                    .multiPart("cover_image", coverImage)
                    .when()
                    .post(ApiConstants.BOOKS);
        } else {
            return RestUtil.post(ApiConstants.BOOKS, book, token);
        }
    }

    public static Response getAllBooks(String token) {
        return RestUtil.get(ApiConstants.BOOKS, token);
    }

    public static Response getBookById(int id, String token) {
        return RestUtil.get(ApiConstants.BOOKS + id, token);
    }

    public static Response updateBook(int id, Book book, String token) {
        return RestUtil.put(ApiConstants.BOOKS + id, book, token);
    }

    public static Response deleteBook(int id, String token) {
        return RestUtil.delete(ApiConstants.BOOKS + id, token);
    }

    public static Response uploadBookCover(File imageFile, String token) {
        return RestAssured.given()
                .baseUri(ApiConstants.BASE_URI)
                .contentType(ContentType.MULTIPART)
                .auth().oauth2(token)
                .multiPart("cover_image", imageFile)
                .when()
                .post(ApiConstants.BOOKS);
    }

    public static Response getBooksByAuthor(String author, String token) {
        return RestUtil.get(ApiConstants.BOOKS + "?author=" + author, token);
    }

    public static Response getBooksByYearRange(int yearMin, int yearMax, String token) {
        return RestUtil.get(ApiConstants.BOOKS + "?year_min=" + yearMin + "&year_max=" + yearMax, token);
    }

    public static Response getBooksByKeyword(String keyword, String token) {
        return RestUtil.get(ApiConstants.BOOKS + "?q=" + keyword, token);
    }

    public static Response getBooksByFilters(String author, int yearMin, int yearMax, String keyword, String token) {
        String url = ApiConstants.BOOKS + "?author=" + author + "&year_min=" + yearMin + "&year_max=" + yearMax + "&q=" + keyword;
        return RestUtil.get(url, token);
    }

    public static Response getRecommendations(String userEmail, String token) {
        return RestUtil.get(ApiConstants.BOOKS + "recommendations/?user_email=" + userEmail, token);
    }

    public static Response getBookHistory(int bookId, String token) {
        return RestUtil.get(ApiConstants.BOOKS + bookId + "/history/", token);
    }
}
