package com.bookStore.service;

import com.bookStore.base.User;
import com.bookStore.config.ApiConstants;
import com.bookStore.utils.ApiRequestUtil;
import io.restassured.response.Response;

public class SignUpService {
    public static Response signUp(User user) {
        return ApiRequestUtil.postRequest(user, ApiConstants.SIGNUP_ENDPOINT);
    }
}
