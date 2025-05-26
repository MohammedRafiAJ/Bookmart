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
