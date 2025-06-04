package com.bookstore.hooks;

import io.cucumber.java.AfterAll;

public class TestTearDown {

    @AfterAll
    public static void globalTearDown() {
        // ServerManager.stopServer(); // Removed because ServerManager no longer exists
    }
}
