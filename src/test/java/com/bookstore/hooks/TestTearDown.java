package com.bookstore.hooks;

import com.bookStore.utils.ServerManager;
import io.cucumber.java.AfterAll;

public class TestTearDown {

    @AfterAll
    public static void globalTearDown() {
        ServerManager.stopServer();
    }
}
