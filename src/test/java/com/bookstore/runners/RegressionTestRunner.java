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
        "html:target/extent-report.html",
        "json:target/cucumber.json"
    },
    tags = "@regression",
    monochrome = true
)
public class RegressionTestRunner {
}
