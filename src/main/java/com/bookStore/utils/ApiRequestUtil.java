package com.bookStore.utils;

import com.bookStore.config.ApiConstants;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class ApiRequestUtil {

    public static Response postRequest(Object body, String endpoint) {
        return given()
                .baseUri(ApiConstants.BASE_URI)
                .contentType(ApiConstants.CONTENT_TYPE)
                .body(body)
                .when()
                .post(endpoint);
    }
}
