package com.bookstore.runners;

import org.junit.runner.RunWith;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

@RunWith(Cucumber.class)
@CucumberOptions(
    features = "src/test/resources/features",
    glue = "com.bookstore.stepdefs",
    plugin = {
        "pretty",
        "html:target/smoke-report.html",
        "json:target/smoke-cucumber.json"
    },
    tags = "@smoke",
    monochrome = true
)
public class SmokeTestRunner {
}
