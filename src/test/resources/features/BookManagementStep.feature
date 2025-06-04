@BookManagementFeature @regression
Feature: Book Management API Validations

  Background:
    Given a user signs up and logs in successfully

  @CreateBook @regression @smoke
  Scenario Outline: Create a book with valid details
    Given a book payload with name "<name>", author "<author>", year <year>, and summary "<summary>" is prepared
    When user sends a request to create a new book
    Then validate book creation response code is 200 and response contains book details

    Examples:
      | name         | author   | year | summary         |
      | MyBook1      | Alice    | 2024 | A story begins  |
      | AutomationQA | Bob      | 2025 | Test coverage   |

  @CreateBookMissingName @regression
  Scenario Outline: Attempt to create a book with missing name
    Given a book payload missing name with id <id>, author "<author>", year <year>, and summary "<summary>" is prepared
    When user sends a request to create a new book
    Then validate book creation fails with code <expectedCode> and error message contains "<errorText>"

    Examples:
      | id | author | year | summary        | expectedCode | errorText              |
      | 1  | Alice  | 2024 | Missing title  | 500          | Internal Server Error  |

  @FetchAllBooks @regression
  Scenario: Fetch all books and validate list
    When user fetches all books
    Then verify response code is 200 and list contains the book name "MyBook1"

  @FetchBookById @regression @smoke
  Scenario: Fetch book by valid ID
    Given a book payload with name "FetchTestBook", author "QA", year 2023, and summary "To be fetched" is prepared
    When user sends a request to create a new book
    And user fetches book by valid ID
    Then verify single book fetch response code is 200 and book name is "FetchTestBook"

  @FetchBookByInvalidId @regression
  Scenario: Fetch book using invalid ID
    When user fetches book by invalid ID
    Then validate not found response with code 404 and message contains "not found"

  @BookWithImage @regression
  Scenario: Create a book with cover image
    Given a book payload with name "BookWithImage", author "Alice", year 2024, summary "With cover", and cover image "src/test/resources/sample.jpg" is prepared
    When user sends a request to create a new book with image

  @SearchByAuthor @regression
  Scenario: Search books by author
    When user searches for books by author "Alice"
    Then verify the search result contains book name "MyBook1"

  @SearchByYearRange @regression
  Scenario: Search books by year range
    When user searches for books published between 2024 and 2025
    Then verify the search result contains book name "MyBook1"

  @SearchByKeyword @regression
  Scenario: Search books by keyword
    When user searches for books with keyword "story"
    Then verify the search result contains book name "MyBook1"

  @SearchByAllFilters @regression
  Scenario: Search books by all filters
    When user searches for books by author "Alice", year range 2024-2024, and keyword "story"
    Then verify the search result contains book name "MyBook1"

  @ReviewBook @regression
  Scenario: Post and fetch a review for a book
    Given a book payload with name "ReviewBook", author "Alice", year 2024, and summary "A book to review" is prepared
    When user sends a request to create a new book
    And user posts a review with rating 5 and text "Excellent book!" for the created book
    And user fetches reviews for the created book
    When user posts a review with rating 3 and text "Good but not great" for the created book
    And user fetches average rating for the created book

  @Recommendations @regression
  Scenario: Get personalized book recommendations
    When user fetches book recommendations
    

  @AuditLog @regression
  Scenario: Audit log for book update
    Given a book payload with name "AuditBook", author "Alice", year 2024, and summary "To be updated" is prepared
    When user sends a request to create a new book
    # Simulate update (reuse createBookRequest for simplicity)
    When user sends a request to create a new book
    And user fetches audit logs for the created book
