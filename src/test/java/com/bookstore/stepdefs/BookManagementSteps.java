package com.bookstore.stepdefs;

import com.bookStore.base.Book;
import com.bookStore.base.User;
import com.bookStore.base.Review;
import com.bookStore.service.BookService;
import com.bookStore.service.SignUpService;
import com.bookStore.service.SignInService;
import com.bookStore.service.ReviewService;
import io.qameta.allure.Allure;
import io.cucumber.java.en.*;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import static org.junit.Assert.*;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;

public class BookManagementSteps {

    private Response response;
    private Book book;
    private int createdBookId;
    private String accessToken;
    private final String email = "bookflow_user_" + System.currentTimeMillis() + "@mail.com";
    private final String password = "Book@123";
    private File coverImageFile;
    private Review review;

    @Given("a user signs up and logs in successfully")
    public void signUpAndLogin() {
        User user = new User((int)(System.currentTimeMillis() % 100000), email, password);
        System.out.println("[DEBUG] Signup payload: " + user);
        Response signUpResp = SignUpService.signUp(user);
        System.out.println("[DEBUG] Signup response status: " + signUpResp.getStatusCode());
        System.out.println("[DEBUG] Signup response body: " + signUpResp.getBody().asString());
        assertEquals(200, signUpResp.getStatusCode());
        Allure.step("User Signup: expected 200, actual " + signUpResp.getStatusCode() + ". User created successfully. Response: " + signUpResp.getBody().asString());

        Response loginResp = SignInService.login(user);
        System.out.println("[DEBUG] Login response status: " + loginResp.getStatusCode());
        System.out.println("[DEBUG] Login response body: " + loginResp.getBody().asString());
        accessToken = JsonPath.from(loginResp.getBody().asString()).getString("access_token");
        assertEquals(200, loginResp.getStatusCode());
        Allure.step("User Login: expected 200, actual " + loginResp.getStatusCode() + ". Login successful. Response: " + loginResp.getBody().asString());
    }

    @Given("a book payload with name {string}, author {string}, year {int}, and summary {string} is prepared")
    public void prepareBookPayload(String name, String author, int year, String summary) {
        book = new Book(name, author, year, summary, null);
        coverImageFile = null;
        Allure.step("Prepared book payload: " + book);
    }

    @Given("a book payload with name {string}, author {string}, year {int}, summary {string}, and cover image {string} is prepared")
    public void prepareBookPayloadWithImage(String name, String author, int year, String summary, String imagePath) {
        book = new Book(name, author, year, summary, null);
        coverImageFile = new File(imagePath);
        Allure.step("Prepared book payload with image: " + book + ", image: " + imagePath);
    }

    @When("user sends a request to create a new book")
    public void createBookRequest() {
        System.out.println("[DEBUG] Book creation payload: " + book);
        response = BookService.createBook(book, accessToken, coverImageFile);
        String body = response.getBody().asString();
        int status = response.getStatusCode();
        System.out.println("[DEBUG] Book creation response status: " + status);
        System.out.println("[DEBUG] Book creation response body: " + body);
        Allure.step("Book creation request response: Status: " + status + ", Body: " + body);
        if (status == 200 && body != null && !body.trim().isEmpty()) {
            createdBookId = JsonPath.from(body).getInt("id");
        }
    }

    @When("user sends a request to create a new book with image")
    public void createBookRequestWithImage() {
        System.out.println("[DEBUG] Book creation payload: " + book + ", image: " + coverImageFile);
        response = BookService.createBook(book, accessToken, coverImageFile);
        String body = response.getBody().asString();
        int status = response.getStatusCode();
        System.out.println("[DEBUG] Book creation response status: " + status);
        System.out.println("[DEBUG] Book creation response body: " + body);
        Allure.step("Book creation with image response: Status: " + status + ", Body: " + body);
        if (status == 200 && body != null && !body.trim().isEmpty()) {
            createdBookId = JsonPath.from(body).getInt("id");
        }
    }

    @Then("validate book creation response code is {int} and response contains book details")
    public void validateCreateBookResponse(int expectedCode) {
        int actualCode = response.getStatusCode();
        String body = response.getBody().asString();
        if (actualCode != expectedCode) {
            Allure.step("Unexpected status code. Expected: " + expectedCode + ", Actual: " + actualCode + ", Body: " + body);
            fail("Unexpected status code. Body: " + body);
        }
        if (body == null || !body.trim().startsWith("{")) {
            Allure.step("Response is not valid JSON. Body: " + body);
            fail("Response is not valid JSON. Body: " + body);
        }
        String name = JsonPath.from(body).getString("name");
        boolean passed = actualCode == expectedCode && name.equals(book.getName());
        Allure.step("Book Creation: expected " + expectedCode + ", actual " + actualCode + ", name: " + name);
        assertEquals(expectedCode, actualCode);
        assertEquals(book.getName(), name);
    }

    @When("user fetches book by valid ID")
    public void fetchBookByValidId() {
        response = BookService.getBookById(createdBookId, accessToken);
        Allure.step("Fetching book by valid ID: " + createdBookId + ", Response Body: " + response.getBody().asString());
    }

    @Then("verify single book fetch response code is {int} and book name is {string}")
    public void validateSingleBookResponse(int expectedCode, String expectedName) {
        int actualCode = response.getStatusCode();
        String body = response.getBody().asString();
        if (actualCode != expectedCode) {
            Allure.step("Unexpected status code. Expected: " + expectedCode + ", Actual: " + actualCode + ", Body: " + body);
            fail("Unexpected status code. Body: " + body);
        }
        if (body == null || !body.trim().startsWith("{")) {
            Allure.step("Response is not valid JSON. Body: " + body);
            fail("Response is not valid JSON. Body: " + body);
        }
        String name = JsonPath.from(body).getString("name");
        boolean passed = actualCode == expectedCode && expectedName.equals(name);
        Allure.step("Fetch Book by ID: expected " + expectedCode + ", actual " + actualCode + ", expectedName: " + expectedName + ", actualName: " + name);
        assertEquals(expectedCode, actualCode);
        assertEquals(expectedName, name);
    }

    @Given("a book payload missing name with id {int}, author {string}, year {int}, and summary {string} is prepared")
    public void a_book_payload_missing_name_with_id_author_year_and_summary_is_prepared(Integer id, String author, Integer year, String summary) {
        book = new Book(null, author, year, summary, null); // name is null
        book.setId(id);
        coverImageFile = null;
        Allure.step("Prepared book payload with missing name: " + book);
    }

    @Then("validate book creation fails with code {int} and error message contains {string}")
    public void validate_book_creation_fails_with_code_and_error_message_contains(Integer expectedCode, String errorText) {
        int actualCode = response.getStatusCode();
        String body = response.getBody().asString();
        boolean is4xx5xx = expectedCode / 100 >= 4;
        try {
            if (is4xx5xx && actualCode == expectedCode) {
                String actualMsg = body;
                if (body.contains("{")) {
                    try {
                        actualMsg = JsonPath.from(body).getString("detail");
                    } catch (Exception ignored) {}
                }
                assertTrue("Expected error to contain '" + errorText + "', but got: '" + actualMsg + "'", actualMsg.toLowerCase().contains(errorText.toLowerCase()));
                Allure.step("Expected failure received. Message: " + actualMsg);
            } else {
                Allure.step("Unexpected status code. Expected: " + expectedCode + ", Actual: " + actualCode);
                fail("Unexpected status code. Body: " + body);
            }
        } catch (Exception e) {
            Allure.step("Exception: " + e.getMessage() + ", Expected: " + expectedCode + ", Actual: " + actualCode + ", Body: " + body);
            throw e;
        }
    }

    @When("user fetches all books")
    public void user_fetches_all_books() {
        response = BookService.getAllBooks(accessToken);
        Allure.step("Fetched all books. Response Body: " + response.getBody().asString());
    }

    @Then("verify response code is {int} and list contains the book name {string}")
    public void verify_response_code_is_and_list_contains_the_book_name(Integer expectedCode, String bookName) {
        int actualCode = response.getStatusCode();
        String body = response.getBody().asString();
        boolean containsBook = body.contains(bookName);
        Allure.step("Fetch All Books: expected " + expectedCode + ", actual " + actualCode + ", bookName: " + bookName + ", contains: " + containsBook);
        assertEquals(expectedCode.intValue(), actualCode);
        assertTrue("Book list does not contain expected book name: " + bookName, containsBook);
    }

    @When("user fetches book by invalid ID")
    public void user_fetches_book_by_invalid_id() {
        int invalidId = -9999;
        response = BookService.getBookById(invalidId, accessToken);
        Allure.step("Fetching book by invalid ID: " + invalidId + ", Response Body: " + response.getBody().asString());
    }

    @Then("validate not found response with code {int} and message contains {string}")
    public void validate_not_found_response_with_code_and_message_contains(Integer expectedCode, String errorText) {
        int actualCode = response.getStatusCode();
        String body = response.getBody().asString();
        boolean is4xx5xx = expectedCode / 100 >= 4;
        try {
            if (is4xx5xx && actualCode == expectedCode) {
                String actualMsg = body;
                if (body.contains("{")) {
                    try {
                        actualMsg = JsonPath.from(body).getString("detail");
                    } catch (Exception ignored) {}
                }
                assertTrue("Expected error to contain '" + errorText + "', but got: '" + actualMsg + "'", actualMsg.toLowerCase().contains(errorText.toLowerCase()));
                Allure.step("Expected not found received. Message: " + actualMsg);
            } else {
                Allure.step("Unexpected status code. Expected: " + expectedCode + ", Actual: " + actualCode);
                fail("Unexpected status code. Body: " + body);
            }
        } catch (Exception e) {
            Allure.step("Exception: " + e.getMessage() + ", Expected: " + expectedCode + ", Actual: " + actualCode + ", Body: " + body);
            throw e;
        }
    }

    @Then("validate book creation response contains cover image URL and image is downloadable")
    public void validateBookCoverImageUrl() throws Exception {
        String body = response.getBody().asString();
        String imageUrl = JsonPath.from(body).getString("cover_image_url");
        assertNotNull("Cover image URL should not be null", imageUrl);
        Allure.step("Book cover image URL: " + imageUrl);
        // Download the image and check response code
        URL url = new URL("http://127.0.0.1:8000" + imageUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        int code = conn.getResponseCode();
        assertEquals(200, code);
        Allure.step("Book cover image is downloadable, HTTP code: " + code);
    }

    @When("user searches for books by author {string}")
    public void user_searches_books_by_author(String author) {
        response = BookService.getBooksByAuthor(author, accessToken);
        Allure.step("Searched books by author: " + author + ", Response: " + response.getBody().asString());
    }

    @When("user searches for books published between {int} and {int}")
    public void user_searches_books_by_year_range(int yearMin, int yearMax) {
        response = BookService.getBooksByYearRange(yearMin, yearMax, accessToken);
        Allure.step("Searched books by year range: " + yearMin + "-" + yearMax + ", Response: " + response.getBody().asString());
    }

    @When("user searches for books with keyword {string}")
    public void user_searches_books_by_keyword(String keyword) {
        response = BookService.getBooksByKeyword(keyword, accessToken);
        Allure.step("Searched books by keyword: " + keyword + ", Response: " + response.getBody().asString());
    }

    @When("user searches for books by author {string}, year range {int}-{int}, and keyword {string}")
    public void user_searches_books_by_all_filters(String author, int yearMin, int yearMax, String keyword) {
        response = BookService.getBooksByFilters(author, yearMin, yearMax, keyword, accessToken);
        Allure.step("Searched books by all filters. Author: " + author + ", Year: " + yearMin + "-" + yearMax + ", Keyword: " + keyword + ", Response: " + response.getBody().asString());
    }

    @Then("verify the search result contains book name {string}")
    public void verify_search_result_contains_book(String bookName) {
        String body = response.getBody().asString();
        assertTrue("Search result does not contain expected book name: " + bookName, body.contains(bookName));
        Allure.step("Verified search result contains book: " + bookName);
    }

    @When("user posts a review with rating {int} and text {string} for the created book")
    public void user_posts_review_for_created_book(int rating, String reviewText) {
        review = new Review(createdBookId, email, rating, reviewText);
        response = ReviewService.postReview(createdBookId, review, accessToken);
        Allure.step("Posted review for book ID: " + createdBookId + ", rating: " + rating + ", text: " + reviewText);
    }

    @When("user fetches reviews for the created book")
    public void user_fetches_reviews_for_created_book() {
        response = ReviewService.getReviews(createdBookId, accessToken);
        Allure.step("Fetched reviews for book ID: " + createdBookId + ", Response: " + response.getBody().asString());
    }

    @Then("verify the review response contains text {string} and rating {int}")
    public void verify_review_response_contains_text_and_rating(String reviewText, int rating) {
        String body = response.getBody().asString();
        assertNotNull("Response body should not be null", body);
        assertTrue("Response should be a JSON array", body.trim().startsWith("["));
        
        JsonPath jsonPath = JsonPath.from(body);
        boolean found = false;
        for (int i = 0; i < jsonPath.getList("$").size(); i++) {
            String text = jsonPath.getString("[" + i + "].review_text");
            int reviewRating = jsonPath.getInt("[" + i + "].rating");
            if (text.equals(reviewText) && reviewRating == rating) {
                found = true;
                break;
            }
        }
        assertTrue("Review response does not contain expected text and rating", found);
        Allure.step("Review validation: Found review with text '" + reviewText + "' and rating " + rating);
    }

    @When("user fetches average rating for the created book")
    public void user_fetches_average_rating_for_created_book() {
        response = ReviewService.getAverageRating(createdBookId, accessToken);
        Allure.step("Fetched average rating for book ID: " + createdBookId + ", Response: " + response.getBody().asString());
    }

    @Then("verify the average rating is {double}")
    public void verify_average_rating_is(double expectedAvg) {
        String body = response.getBody().asString();
        assertNotNull("Response body should not be null", body);
        assertTrue("Response should be a JSON object", body.trim().startsWith("{"));
        
        JsonPath jsonPath = JsonPath.from(body);
        double actualAvg = jsonPath.getDouble("average_rating");
        assertEquals("Average rating mismatch", expectedAvg, actualAvg, 0.01);
        Allure.step("Average rating validation: Expected " + expectedAvg + ", got " + actualAvg);
    }

    @When("user fetches book recommendations")
    public void user_fetches_book_recommendations() {
        response = BookService.getRecommendations(email, accessToken);
        Allure.step("Fetched book recommendations for user: " + email + ", Response: " + response.getBody().asString());
    }

    @Then("verify the recommendations contain book name {string}")
    public void verify_recommendations_contain_book(String bookName) {
        String body = response.getBody().asString();
        assertNotNull("Response body should not be null", body);
        assertTrue("Response should be a JSON array", body.trim().startsWith("["));
        
        JsonPath jsonPath = JsonPath.from(body);
        boolean found = false;
        for (int i = 0; i < jsonPath.getList("$").size(); i++) {
            String name = jsonPath.getString("[" + i + "].name");
            if (name.equals(bookName)) {
                found = true;
                break;
            }
        }
        assertTrue("Recommendations do not contain expected book name: " + bookName, found);
        Allure.step("Recommendations validation: Found book '" + bookName + "' in recommendations");
    }

    @When("user fetches audit logs for the created book")
    public void user_fetches_audit_logs_for_the_created_book() {
        response = BookService.getBookHistory(createdBookId, accessToken);
        Allure.step("Fetched audit logs for book " + createdBookId + ", Response: " + response.getBody().asString());
    }

    @Then("verify the audit log contains action {string}")
    public void verify_the_audit_log_contains_action(String action) {
        String body = response.getBody().asString();
        assertNotNull("Response body should not be null", body);
        assertTrue("Response should be a JSON array", body.trim().startsWith("["));
        
        JsonPath jsonPath = JsonPath.from(body);
        boolean found = false;
        for (int i = 0; i < jsonPath.getList("$").size(); i++) {
            String logAction = jsonPath.getString("[" + i + "].action");
            if (action.equals(logAction)) {
                found = true;
                break;
            }
        }
        assertTrue("Audit log does not contain expected action: " + action, found);
        Allure.step("Audit log validation: Found action '" + action + "' in logs");
    }

}
