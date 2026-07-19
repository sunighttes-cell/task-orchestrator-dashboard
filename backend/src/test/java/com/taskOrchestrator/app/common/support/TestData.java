package com.taskOrchestrator.app.common.support;

public final class TestData {

    private TestData() {
        // Utility class
    }

    //Users
    public static final String USERNAME = "testuser";
    public static final String USERNAME_TWO = "anotheruser";
    public static final String INVALID_USERNAME = "unknownuser";

    //Email
    public static final String EMAIL = "testuser@example.com";
    public static final String EMAIL_TWO = "another@example.com";
    public static final String INVALID_EMAIL = "invalid-email";

    //Names
    public static final String FULL_NAME = "Test User";
    public static final String FULL_NAME_TWO = "Another User";
    public static final String EMPTY_FULL_NAME = "";

    //Passwords
    public static final String PASSWORD = "Password123!";
    public static final String PASSWORD_TWO = "AnotherPassword123!";
    public static final String INVALID_PASSWORD = "bad";
    public static final String WRONG_PASSWORD = "WrongPassword123!";

    //JWT // These are only placeholder values for mocked tests. Integration tests should generate real JWTs using JwtUtil.
    public static final String ACCESS_TOKEN = "mock-access-token";
    public static final String REFRESH_TOKEN = "mock-refresh-token";
    public static final String INVALID_REFRESH_TOKEN = "invalid-refresh-token";
    public static final String INVALID_ACCESS_TOKEN = "invalid-access-token";

    //Register Requests
    public static final String VALID_REGISTER_JSON = """
        {
          "username":"testuser",
          "email":"testuser@example.com",
          "fullName":"Test User",
          "password":"Password123!"
        }
        """;

    public static final String DUPLICATE_USERNAME_REGISTER_JSON = """
        {
          "username":"testuser",
          "email":"another@example.com",
          "fullName":"Test User",
          "password":"Password123!"
        }
        """;

    public static final String DUPLICATE_EMAIL_REGISTER_JSON = """
        {
          "username":"anotheruser",
          "email":"testuser@example.com",
          "fullName":"Test User",
          "password":"Password123!"
        }
        """;

    public static final String INVALID_EMAIL_REGISTER_JSON = """
        {
          "username":"testuser",
          "email":"invalid-email",
          "fullName":"Test User",
          "password":"Password123!"
        }
        """;

    public static final String INVALID_PASSWORD_REGISTER_JSON = """
        {
          "username":"testuser",
          "email":"testuser@example.com",
          "fullName":"Test User",
          "password":"bad"
        }
        """;

    // Login Requests
    public static final String VALID_LOGIN_JSON = """
        {
          "username":"testuser",
          "password":"Password123!"
        }
        """;

    public static final String INVALID_LOGIN_JSON = """
        {
          "username":"unknownuser",
          "password":"WrongPassword123!"
        }
        """;

    //Refresh Requests
    public static final String VALID_REFRESH_JSON = """
        {
          "refreshToken":"mock-refresh-token"
        }
        """;

    public static final String INVALID_REFRESH_JSON = """
        {
          "refreshToken":"invalid-refresh-token"
        }
        """;
}