// Auth Step Definitions (Updated with Allure Annotations)
package com.bookstore.stepdefs;

import com.bookStore.base.User;
import com.bookStore.service.SignInService;
import com.bookStore.service.SignUpService;
import io.cucumber.java.en.*;
import io.qameta.allure.*;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import java.util.*;

import static org.junit.Assert.*;

@Epic("User Authentication")
@Feature("Signup and Login")
public class AuthSteps {

    private Response response;
    private int uniqueId;
    private String uniqueUsername;
    private String password;
    private Map<String, String> usernameMap = new HashMap<>();
    private Map<String, Integer> idMap = new HashMap<>();

    @Story("Login without signing up")
    @Severity(SeverityLevel.CRITICAL)
    @When("user tried to login with noSignUpUser credentials into book store system")
    public void loginWithoutSignup() {
        User user = new User(generateRandomId(), generateRandomUsername(), generateRandomPassword());
        response = SignInService.login(user);
    }

    @Step("Verify login response code is {expectedCode} and message contains '{expectedMsg}'")
    @Then("verify the login response code is {int} and message contains {string}")
    public void validateLogin(int expectedCode, String expectedMsg) {
        int actualCode = response.getStatusCode();
        String body = response.asString();

        boolean is2xx = expectedCode / 100 == 2;
        boolean is4xx5xx = expectedCode / 100 >= 4;

        try {
            if (is2xx && actualCode == expectedCode) {
                String token = JsonPath.from(body).getString("access_token");
                assertNotNull("Expected token but was null", token);
            } else if (is4xx5xx && actualCode == expectedCode) {
                String actualMsg = JsonPath.from(body).getString("detail");
                assertTrue(actualMsg.toLowerCase().contains(expectedMsg.toLowerCase()));
            } else {
                fail("Unexpected response. Expected: " + expectedCode + ", Actual: " + actualCode);
            }
        } catch (Exception e) {
            throw e;
        }
    }

    @Story("Login with missing parameters")
    @Severity(SeverityLevel.NORMAL)
    @When("user tried to login with missingParam credentials into book store system")
    public void loginWithMissingParams() {
        User user = new User(0, "", "vakq");
        response = SignInService.login(user);
    }

    @Story("Signup user preparation")
    @Severity(SeverityLevel.MINOR)
    @Given("Sign up to the book store as the new user with email and password")
    public void prepareSignUpUser() {
        uniqueId = generateRandomId();
        uniqueUsername = generateRandomUsername();
        password = generateRandomPassword();
    }

    @Story("Successful signup")
    @Severity(SeverityLevel.CRITICAL)
    @When("do the sign up with valid credentials")
    public void doValidSignup() {
        User user = new User(uniqueId, uniqueUsername, password);
        response = SignUpService.signUp(user);
    }

    @Step("Validate signup response code is {expectedCode} and message contains '{expectedMessage}'")
    @Then("validate signup response code is {int} and message contains {string}")
    public void validateSignupSuccess(int expectedCode, String expectedMessage) {
        int actualStatus = response.getStatusCode();
        String responseBody = response.asString();

        boolean is2xx = expectedCode / 100 == 2;
        boolean is4xx5xx = expectedCode / 100 >= 4;

        try {
            if (is2xx && actualStatus == expectedCode) {
                String actualMsg = JsonPath.from(responseBody).getString("message");
                assertEquals(expectedMessage, actualMsg);
            } else if (is4xx5xx && actualStatus == expectedCode) {
                String actualMsg = JsonPath.from(responseBody).getString("detail");
                assertTrue(actualMsg.toLowerCase().contains(expectedMessage.toLowerCase()));
            } else {
                fail("Unexpected status code. Body: " + responseBody);
            }
        } catch (Exception e) {
            throw e;
        }
    }

    @Story("Login with valid credentials")
    @Severity(SeverityLevel.BLOCKER)
    @When("user tried to login with valid credentials into book store system")
    public void loginWithValidCreds() {
        User user = new User(uniqueId, uniqueUsername, password);
        response = SignInService.login(user);
    }

    @Story("Prepare dynamic user")
    @Given("I prepare a unique user with email prefix {string} and password {string}")
    public void prepareDynamicUser(String prefix, String pass) {
        int id = generateRandomId();
        String username = prefix + "_" + generateRandomUsername();
        usernameMap.put(prefix, username);
        idMap.put(prefix, id);
        User user = new User(id, username, pass);
        response = SignUpService.signUp(user);
    }

    @Story("Login dynamic user")
    @When("user tries to login using the same email prefix {string} and password {string}")
    public void loginDynamicUser(String prefix, String pass) {
        String username = usernameMap.get(prefix);
        int id = idMap.get(prefix);
        User user = new User(id, username, pass);
        response = SignInService.login(user);
    }

    @Story("Signup with same email")
    @When("I sign up with the same email and a new ID using prefix {string} and password {string}")
    public void signupWithSameEmail(String prefix, String pwd) {
        String username = usernameMap.get(prefix);
        User user = new User(generateRandomId(), username, pwd);
        response = SignUpService.signUp(user);
    }

    @Story("Signup with same ID")
    @When("I sign up with the same ID and a new email using prefix {string} and password {string}")
    public void signupWithSameId(String prefix, String pwd) {
        int id = idMap.get(prefix);
        String username = generateRandomUsername();
        User user = new User(id, username, pwd);
        response = SignUpService.signUp(user);
    }

    @Story("Signup with duplicate credentials")
    @And("do the sign up with old credentials")
    public void doSignupWithOldCredentials() {
        User user = new User(uniqueId + 1, uniqueUsername, password);
        response = SignUpService.signUp(user);
    }

    @Story("Signup with missing email")
    @And("do the sign up with newPasswordOnly credentials")
    public void doSignupWithPasswordOnly() {
        User user = new User(uniqueId + 2, "", password);
        response = SignUpService.signUp(user);
    }

    private String generateRandomUsername() {
        return "user_" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String generateRandomPassword() {
        return "Pwd@" + new Random().nextInt(99999);
    }

    private int generateRandomId() {
        return (int)(System.currentTimeMillis() % 100000);
    }
}
