package com.bookStore.utils;

import com.bookStore.config.ApiConstants;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class RestUtil {

    public static Response post(String endpoint, Object body, String token) {
        return given()
                .baseUri(ApiConstants.BASE_URI)
                .contentType(ContentType.JSON)
                .auth().oauth2(token)
                .body(body)
                .when()
                .post(endpoint);
    }

    public static Response get(String endpoint, String token) {
        return given()
                .baseUri(ApiConstants.BASE_URI)
                .auth().oauth2(token)
                .when()
                .get(endpoint);
    }

    public static Response put(String endpoint, Object body, String token) {
        return given()
                .baseUri(ApiConstants.BASE_URI)
                .contentType(ContentType.JSON)
                .auth().oauth2(token)
                .body(body)
                .when()
                .put(endpoint);
    }

    public static Response delete(String endpoint, String token) {
        return given()
                .baseUri(ApiConstants.BASE_URI)
                .auth().oauth2(token)
                .when()
                .delete(endpoint);
    }
}
