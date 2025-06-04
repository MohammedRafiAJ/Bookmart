// Auth Step Definitions (Finalized with validation matrix)
package com.bookstore.stepdefs;

import com.bookStore.base.User;
import com.bookStore.service.SignInService;
import com.bookStore.service.SignUpService;
import io.cucumber.java.en.*;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.qameta.allure.Allure;

import java.util.*;

import static org.junit.Assert.*;

public class AuthSteps {

    private Response response;
    private int uniqueId;
    private String uniqueUsername;
    private String password;
    private Map<String, String> usernameMap = new HashMap<>();
    private Map<String, Integer> idMap = new HashMap<>();

    @When("user tried to login with noSignUpUser credentials into book store system")
    public void loginWithoutSignup() {
        User user = new User(generateRandomId(), generateRandomUsername(), generateRandomPassword());
        response = SignInService.login(user);
        Allure.step("Login attempt with non-existent user");
    }

    @Then("verify the login response code is {int} and message contains {string}")
    public void validateLogin(int expectedCode, String expectedMsg) {
        int actualCode = response.getStatusCode();
        String body = response.getBody().asString();

        boolean is2xx = expectedCode / 100 == 2;
        boolean is4xx5xx = expectedCode / 100 >= 4;

        try {
            if (is2xx && actualCode == expectedCode) {
                String token = JsonPath.from(body).getString("access_token");
                assertNotNull("Expected token but was null", token);
                Allure.step("[PASS] Token found in response: " + token);
            } else if (is4xx5xx && actualCode == expectedCode) {
                String actualMsg = JsonPath.from(body).getString("detail");
                assertTrue("Message mismatch: Expected to contain '" + expectedMsg + "', but got: '" + actualMsg + "'", actualMsg.toLowerCase().contains(expectedMsg.toLowerCase()));
                Allure.step("[PASS] Error message matched: " + actualMsg);
            } else {
                Allure.step("Unexpected status. Expected: " + expectedCode + ", Actual: " + actualCode);
                fail("Unexpected response. Expected: " + expectedCode + ", Actual: " + actualCode + ", Body: " + body);
            }
        } catch (Exception e) {
            Allure.step("Exception: " + e.getMessage() + ", Expected: " + expectedCode + ", Actual: " + actualCode + ", Body: " + body);
            throw e;
        }
    }

    @When("user tried to login with missingParam credentials into book store system")
    public void loginWithMissingParams() {
        User user = new User(0, "", "vakq");
        response = SignInService.login(user);
        Allure.step("Login with missing email parameter");
    }

    @Given("Sign up to the book store as the new user with email and password")
    public void prepareSignUpUser() {
        uniqueId = generateRandomId();
        uniqueUsername = generateRandomUsername();
        password = generateRandomPassword();
        Allure.step("Signup user prepared: " + uniqueUsername);
    }

    @When("do the sign up with valid credentials")
    public void doValidSignup() {
        User user = new User(uniqueId, uniqueUsername, password);
        response = SignUpService.signUp(user);
        Allure.step("Executed signup API for user: " + uniqueUsername);
    }

    @Then("validate signup response code is {int} and message contains {string}")
    public void validateSignupSuccess(int expectedCode, String expectedMessage) {
        int actualStatus = response.getStatusCode();
        String responseBody = response.getBody().asString();

        boolean is2xx = expectedCode / 100 == 2;
        boolean is4xx5xx = expectedCode / 100 >= 4;

        try {
            if (is2xx && actualStatus == expectedCode) {
                String actualMsg = JsonPath.from(responseBody).getString("message");
                assertEquals("Signup success message mismatch", expectedMessage, actualMsg);
                Allure.step("Signup succeeded. Message: " + actualMsg);
            } else if (is4xx5xx && actualStatus == expectedCode) {
                if (responseBody.contains("{")) {
                    String actualMsg = JsonPath.from(responseBody).getString("detail");
                    assertTrue("Expected error to contain '" + expectedMessage + "', but got: '" + actualMsg + "'", actualMsg.toLowerCase().contains(expectedMessage.toLowerCase()));
                    Allure.step("Expected failure received. Message: " + actualMsg);
                } else {
                    assertTrue("Expected error message not structured. Body: " + responseBody, responseBody.toLowerCase().contains(expectedMessage.toLowerCase()));
                    Allure.step("Expected error in raw response: " + responseBody);
                }
            } else {
                Allure.step("Unexpected status code. Expected: " + expectedCode + ", Actual: " + actualStatus);
                fail("Unexpected status code. Body: " + responseBody);
            }
        } catch (Exception e) {
            Allure.step("Exception: " + e.getMessage() + ", Expected: " + expectedCode + ", Actual: " + actualStatus + ", Body: " + responseBody);
            throw e;
        }
    }

    @When("user tried to login with valid credentials into book store system")
    public void loginWithValidCreds() {
        User user = new User(uniqueId, uniqueUsername, password);
        response = SignInService.login(user);
        Allure.step("Login using valid credentials");
    }

    @Given("I prepare a unique user with email prefix {string} and password {string}")
    public void prepareDynamicUser(String prefix, String pass) {
        int id = generateRandomId();
        String username = prefix + "_" + generateRandomUsername();
        usernameMap.put(prefix, username);
        idMap.put(prefix, id);
        User user = new User(id, username, pass);
        response = SignUpService.signUp(user);
        Allure.step("Signup done for user: " + username);
    }

    @When("user tries to login using the same email prefix {string} and password {string}")
    public void loginDynamicUser(String prefix, String pass) {
        String username = usernameMap.get(prefix);
        int id = idMap.get(prefix);
        User user = new User(id, username, pass);
        response = SignInService.login(user);
        Allure.step("Login for user: " + username);
    }

    @When("I sign up with the same email and a new ID using prefix {string} and password {string}")
    public void signupWithSameEmail(String prefix, String pwd) {
        String username = usernameMap.get(prefix);
        User user = new User(generateRandomId(), username, pwd);
        response = SignUpService.signUp(user);
        Allure.step("Signup with same email tried: " + username);
    }

    @When("I sign up with the same ID and a new email using prefix {string} and password {string}")
    public void signupWithSameId(String prefix, String pwd) {
        int id = idMap.get(prefix);
        String username = generateRandomUsername();
        User user = new User(id, username, pwd);
        response = SignUpService.signUp(user);
        Allure.step("Signup with same ID tried: ID=" + id + ", Email=" + username);
    }

    @And("do the sign up with old credentials")
    public void doSignupWithOldCredentials() {
        User user = new User(uniqueId + 1, uniqueUsername, password);
        response = SignUpService.signUp(user);
        Allure.step("Signup with duplicate email: " + user.getEmail());
    }

    @And("do the sign up with newPasswordOnly credentials")
    public void doSignupWithPasswordOnly() {
        User user = new User(uniqueId + 2, "", password);
        response = SignUpService.signUp(user);
        Allure.step("Signup with missing email");
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
