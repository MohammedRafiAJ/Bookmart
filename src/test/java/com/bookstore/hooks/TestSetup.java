package com.bookstore.hooks;
import io.qameta.allure.Allure;

import com.bookStore.utils.ServerManager;

import io.cucumber.java.BeforeAll;

public class TestSetup {

@BeforeAll
public static void globalSetup() {
    // Allure.initReport();  // Removed: method does not exist

    ServerManager.startServer(); // This should now work and log to report
}
}