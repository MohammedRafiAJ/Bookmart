package com.bookStore.service;

import com.bookStore.base.Book;
import com.bookStore.config.ApiConstants;
import com.bookStore.utils.RestUtil;
import io.restassured.response.Response;

public class BookService {

    public static Response createBook(Book book, String token) {
        return RestUtil.post(ApiConstants.BOOKS, book, token);
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
}
