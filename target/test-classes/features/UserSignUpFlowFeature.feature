@UserSignUpFlowFeature @regression
Feature: User Sign In and Sign Up API Validations

  @LoginBeforeSignUp @regression @smoke
  Scenario: Login attempt before signup should return error
    When user tried to login with noSignUpUser credentials into book store system
    Then verify the login response code is 400 and message contains "Incorrect email or password"

  @LoginAPIValidationWithMissingParam @regression
  Scenario: Login attempt with missing parameters should return error
    When user tried to login with missingParam credentials into book store system
    Then verify the login response code is 400 and message contains "Incorrect email or password"

  @SignUpAndLogin @regression @smoke
  Scenario: Sign up as a new user and login with the same credentials
    Given Sign up to the book store as the new user with email and password
    When do the sign up with valid credentials
    Then validate signup response code is 200 and message contains "User created successfully"
    When user tried to login with valid credentials into book store system
    Then verify the login response code is 200 and message contains "access_token"

  @SignUpMultipleUsersWithLogin @regression
  Scenario Outline: Signup and Login with multiple dynamic users
    Given I prepare a unique user with email prefix "<prefix>" and password "<password>"
    Then validate signup response code is 200 and message contains "User created successfully"
    When user tries to login using the same email prefix "<prefix>" and password "<password>"
    Then verify the login response code is 200 and message contains "access_token"

    Examples:
      | prefix   | password     |
      | vakq1    | vakq1        |
      | userapi  | pass123      |
      | qaauto   | Password@1   |

  @SignUpToBookStore @regression
  Scenario: Signup with new email id and verify the success and error response
    Given Sign up to the book store as the new user with email and password
    When do the sign up with valid credentials
    Then validate signup response code is 200 and message contains "User created successfully"
    And do the sign up with old credentials
    Then validate signup response code is 400 and message contains "Email already registered"
    And do the sign up with newPasswordOnly credentials
    Then validate signup response code is 400 and message contains "Email already registered"

  @SignUpDuplicateValidation @regression
  Scenario: Signup validation with duplicate email and same ID
    Given I prepare a unique user with email prefix "vakq" and password "vakq"
    Then validate signup response code is 200 and message contains "User created successfully"
    When I sign up with the same email and a new ID using prefix "vakq" and password "vakq"
    Then validate signup response code is 400 and message contains "Email already registered"
    When I sign up with the same ID and a new email using prefix "vakq" and password "vakq"
    Then validate signup response code is 500 and message contains "Internal Server Error"

  @SignUpMultipleUsers @regression
  Scenario Outline: Signup using multiple dynamic email prefixes and passwords
    Given I prepare a unique user with email prefix "<emailPrefix>" and password "<password>"
    Then validate signup response code is 200 and message contains "User created successfully"

    Examples:
      | emailPrefix | password     |
      | vakq1       | vakq1        |
      | userapi     | pass123      |
      | qaauto      | Password@1   |
