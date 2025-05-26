package com.bookstore.stepdefs;

import com.bookStore.base.Book;
import com.bookStore.base.User;
import com.bookStore.service.BookService;
import com.bookStore.service.SignInService;
import com.bookStore.service.SignUpService;
import io.cucumber.java.en.*;
import io.qameta.allure.*;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import static org.junit.Assert.*;

@Epic("Bookstore API")
@Feature("Book Management")
public class BookManagementSteps {

    private Response response;
    private Book book;
    private int createdBookId;
    private String accessToken;
    private final String email = "bookflow_user_" + System.currentTimeMillis() + "@mail.com";
    private final String password = "Book@123";

    @Given("a user signs up and logs in successfully")
    @Story("User Authentication")
    @Step("Sign up and log in user")
    public void signUpAndLogin() {
        User user = new User((int) (System.currentTimeMillis() % 100000), email, password);
        Response signUpResp = SignUpService.signUp(user);
        assertEquals(200, signUpResp.getStatusCode());

        Allure.step("User signup response: " + signUpResp.asString());

        Response loginResp = SignInService.login(user);
        String loginRespBody = loginResp.asString();
        accessToken = JsonPath.from(loginRespBody).getString("access_token");
        assertEquals(200, loginResp.getStatusCode());

        Allure.step("User login successful. Access token: " + accessToken);
    }

    @Given("a book payload with name {string}, author {string}, year {int}, and summary {string} is prepared")
    @Story("Book Creation")
    @Step("Prepare book payload")
    public void prepareBookPayload(String name, String author, int year, String summary) {
        book = new Book(name, author, year, summary);
        Allure.step("Prepared book payload: " + book);
    }

    // Added missing step definition for missing name payload
    @Given("a book payload missing name with id {int}, author {string}, year {int}, and summary {string} is prepared")
    @Story("Book Creation")
    @Step("Prepare book payload with missing name")
    public void prepareBookPayloadMissingName(int id, String author, int year, String summary) {
        book = new Book(null, author, year, summary); // name is null/missing
        Allure.step("Prepared book payload with missing name: " + book);
    }

    @When("user sends a request to create a new book")
    @Step("Send book creation request")
    public void createBookRequest() {
        response = BookService.createBook(book, accessToken);
        String body = response.asString();
        int status = response.getStatusCode();

        Allure.step("Book creation response: Status = " + status + ", Body = " + body);

        if (status == 200 && body != null && !body.trim().isEmpty()) {
            try {
                createdBookId = JsonPath.from(body).getInt("id");
            } catch (Exception e) {
                Allure.step("Warning: Could not extract book ID from response");
            }
        }
    }

    @Then("validate book creation response code is {int} and response contains book details")
    @Step("Validate book creation response")
    public void validateCreateBookResponse(int expectedCode) {
        int actualCode = response.getStatusCode();
        String body = response.asString();

        Allure.step("Response Status Code: " + actualCode);
        Allure.step("Raw Response Body: " + body);

        assertEquals("Unexpected status code", expectedCode, actualCode);

        // Only validate JSON content if we expect success (200)
        if (expectedCode == 200) {
            String contentType = response.getContentType();
            if (contentType != null && contentType.contains("application/json")) {
                String name;
                try {
                    name = JsonPath.from(body).getString("name");
                } catch (Exception e) {
                    throw new RuntimeException("Failed to parse JSON from response: " + body, e);
                }

                Allure.step("Book name in response: " + name);
                assertEquals("Book name mismatch", book.getName(), name);
            } else {
                throw new RuntimeException("Response is not JSON: " + body);
            }
        }
    }

    // Added missing step definition for validation failures
    @Then("validate book creation fails with code {int} and error message contains {string}")
    @Step("Validate book creation failure")
    public void validateBookCreationFailure(int expectedCode, String expectedMessage) {
        int actualCode = response.getStatusCode();
        String body = response.asString();

        Allure.step("Expected failure code: " + expectedCode + ", Actual: " + actualCode);
        Allure.step("Response body: " + body);

        assertEquals("Unexpected error status code", expectedCode, actualCode);
        assertTrue("Error message should contain: " + expectedMessage, 
                   body.toLowerCase().contains(expectedMessage.toLowerCase()));
    }

    // Added missing step definition for fetching all books
    @When("user fetches all books")
    @Step("Fetch all books")
    public void fetchAllBooks() {
        response = BookService.getAllBooks(accessToken);
        Allure.step("Fetched all books response: " + response.asString());
    }

    // Added missing step definition for validating book list
    @Then("verify response code is {int} and list contains the book name {string}")
    @Step("Validate book list response")
    public void validateBookListResponse(int expectedCode, String expectedBookName) {
        int actualCode = response.getStatusCode();
        String body = response.asString();

        Allure.step("Expected code: " + expectedCode + ", Actual: " + actualCode);
        assertEquals(expectedCode, actualCode);

        // Check if the book name exists in the list
        assertTrue("Book list should contain: " + expectedBookName, 
                   body.contains(expectedBookName));
    }

    @When("user fetches book by valid ID")
    @Step("Fetch book by ID")
    public void fetchBookByValidId() {
        response = BookService.getBookById(createdBookId, accessToken);
        Allure.step("Fetched book response: " + response.asString());
    }

    // Added missing step definition for fetching by invalid ID
    @When("user fetches book by invalid ID")
    @Step("Fetch book by invalid ID")
    public void fetchBookByInvalidId() {
        int invalidId = 99999; // Use a clearly invalid ID
        response = BookService.getBookById(invalidId, accessToken);
        Allure.step("Fetched book by invalid ID response: " + response.asString());
    }

    @Then("verify single book fetch response code is {int} and book name is {string}")
    @Step("Validate single book fetch")
    public void validateSingleBookResponse(int expectedCode, String expectedName) {
        int actualCode = response.getStatusCode();
        String body = response.asString();

        Allure.step("Expected code: " + expectedCode + ", Actual: " + actualCode);
        assertEquals(expectedCode, actualCode);

        if (expectedCode == 200) {
            String name = JsonPath.from(body).getString("name");
            Allure.step("Expected name: " + expectedName + ", Actual: " + name);
            assertEquals(expectedName, name);
        }
    }

    // Added missing step definition for invalid ID validation
    @Then("validate not found response with code {int} and message contains {string}")
    @Step("Validate not found response")
    public void validateNotFoundResponse(int expectedCode, String expectedMessage) {
        int actualCode = response.getStatusCode();
        String body = response.asString();

        Allure.step("Expected not found code: " + expectedCode + ", Actual: " + actualCode);
        Allure.step("Response body: " + body);

        assertEquals("Unexpected not found status code", expectedCode, actualCode);
        assertTrue("Response should contain: " + expectedMessage, 
                   body.toLowerCase().contains(expectedMessage.toLowerCase()));
    }
}