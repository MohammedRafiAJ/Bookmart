
# Bookstore API Automation Project

This repository contains an end-to-end API automation framework built for a FastAPI-based Bookstore application. The tests are written in Java using RestAssured and Cucumber, with reporting via Allure Reports. The project also includes automated server startup and supports CI/CD pipelines.

## Tech Stack

| Layer           | Technology         |
|----------------|---------------------|
| API Backend     | FastAPI            |
| API Automation  | Java + RestAssured |
| Test Framework  | Cucumber (BDD)     |
| Reports         | Allure Reports     |
| Build Tool      | Maven              |
| CI/CD           | GitHub Actions     |

## Project Structure

```
bookstore-api-test/
├── bookstore-main/             # FastAPI backend
│   ├── main.py                 # FastAPI app entry point
│   └── requirements.txt        # Python dependencies
├── src/
│   └── main/java/com/bookStore
│       ├── base/               # Request payload models
│       ├── config/             # Configuration reader
│       ├── service/            # API service classes
│       ├── utils/              # Utilities (reporting, server, etc.)
│   └── test/java/com/bookstore
│       ├── stepdefs/           # Step definitions
│       ├── hooks/              # Hooks for setup/teardown
├── features/                   # Cucumber feature files
│   └── BookManagement.feature
├── pom.xml                     # Maven config
└── README.md                   # Project documentation
```

## Setup & Execution

### 1. Backend Server (Automated)

No need to start the FastAPI server manually. It's launched automatically before the tests begin using Java ProcessBuilder.

Example code:
```java
ProcessBuilder pb = new ProcessBuilder("uvicorn", "main:app", "--reload");
pb.directory(new File("bookstore-main"));
pb.redirectErrorStream(true);
```

### 2. Run Tests

```bash
mvn clean verify
```

- Starts the FastAPI server
- Executes all Cucumber test scenarios
- Generates an HTML test report

## Test Scenarios Covered

### Authentication

| Scenario Description              | Endpoint  | Method |
|----------------------------------|-----------|--------|
| Signup with valid data           | /signup   | POST   |
| Signup with existing email       | /signup   | POST   |
| Signup with missing fields       | /signup   | POST   |
| Login with valid credentials     | /login    | POST   |
| Login before signup              | /login    | POST   |
| Login with missing fields        | /login    | POST   |

### Book Management

| Scenario Description              | Endpoint         | Method  |
|----------------------------------|------------------|---------|
| Create book with valid details   | /books/          | POST    |
| Create book with missing name    | /books/          | POST    |
| Fetch all books                  | /books/          | GET     |
| Fetch book by valid ID           | /books/{id}      | GET     |
| Fetch book by invalid ID         | /books/{id}      | GET     |
| Update existing book             | /books/{id}      | PUT     |
| Update non-existing book         | /books/{id}      | PUT     |
| Delete book by ID                | /books/{id}      | DELETE  |
| Delete non-existing book         | /books/{id}      | DELETE  |

## Reporting

After test execution, view the detailed report:

```yaml
- name: Run Tests
  run: pytest --alluredir=allure-results

- name: Generate Allure Report
  run: allure generate allure-results --clean -o allure-report

- name: Run the allure-report( To run HTML file )
  run: allure serve

```

Includes:
- Scenario-wise results
- Request and response logs
- Assertions and validations

## CI/CD Pipeline

The project supports CI/CD integration using GitHub Actions.

### Sample GitHub Actions Workflow

```yaml
name: CI Pipeline

on: [push, pull_request]

jobs:
  test:
    runs-on: windows-latest

    steps:
    - uses: actions/checkout@v3

    - name: Set up Java
      uses: actions/setup-java@v3
      with:
        java-version: '17'

    - name: Install Python dependencies
      run: |
        cd bookstore-main
        pip install -r requirements.txt

    - name: Start FastAPI server
      run: |
        start uvicorn main:app --reload
      working-directory: bookstore-main

    - name: Run API Tests
      run: mvn clean verify


```

## Author

**Mohammed Rafi A J**  
Senior Test Engineer  
Expertise: Java, API Testing, RestAssured, Cucumber, Selenium
