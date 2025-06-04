package com.bookStore.service;

import com.bookStore.base.User;
import com.bookStore.config.ApiConstants;
import com.bookStore.utils.ApiRequestUtil;
import io.restassured.response.Response;

public class SignInService {
    public static Response login(User user) {
        return ApiRequestUtil.postRequest(user, ApiConstants.LOGIN_ENDPOINT);
    }
}
