package com.bookstore.hooks;
import io.qameta.allure.Allure;
import io.cucumber.java.Before;
import io.cucumber.java.After;
import io.cucumber.java.Scenario;

public class Hooks {

	 @Before
	    public void beforeScenario(Scenario scenario) {
            Allure.step("Starting Scenario: " + scenario.getName());
	    }

    @After
    public void afterScenario(Scenario scenario) {
        if (scenario.isFailed()) {
            Allure.step("Scenario failed: " + scenario.getName());
        } else {
            Allure.step("Scenario passed: " + scenario.getName());
        }
    }
}
