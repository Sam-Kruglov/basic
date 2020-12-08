package com.samkruglov.base.validation;

import com.samkruglov.base.api.UserController;
import com.samkruglov.base.api.view.mapper.UserMapper;
import com.samkruglov.base.api.view.request.ChangeUserDto;
import com.samkruglov.base.api.view.request.CreateUserDto;
import com.samkruglov.base.config.ValidationTest;
import com.samkruglov.base.service.UserService;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Import(UserController.class)
public class UserValidationTest extends ValidationTest {

    @MockBean UserService service;
    @MockBean UserMapper  mapper;

    @Nested
    class create_user {
        String firstName;
        String lastName;
        String email;
        String password;

        @BeforeEach
        void setUp() {
            firstName = "john";
            lastName = "smith";
            email = "john.smith@company.com";
            password = "pw";
        }

        void createUser(String... expectedInvalidFields) {
            sendAndAssertFields(
                    client -> client.post()
                                    .uri("/api/users")
                                    .bodyValue(new CreateUserDto(firstName, lastName, email, password))
                                    .exchange(),
                    expectedInvalidFields
            );
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = "invalid")
        void invalid_email(String invalidEmail) {
            email = invalidEmail;
            createUser("email");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @MethodSource("com.samkruglov.base.validation.UserValidationTest#very_long_name_factory")
        void invalid_first_and_last_name(String invalidName) {
            firstName = invalidName;
            lastName = invalidName;
            createUser("firstName", "lastName");
        }

        @ParameterizedTest
        @NullAndEmptySource
        void invalid_password(String invalidPassword) {
            password = invalidPassword;
            createUser("password");
        }
    }

    @Nested
    class get_user {

        String email;

        @BeforeEach
        void setUp() {
            email = "john.smith@company.com";
        }

        void getUser(String... expectedInvalidFields) {
            sendAndAssertFields(
                    client -> client.get().uri("/api/users/{email}", email).exchange(),
                    expectedInvalidFields
            );
        }

        @Test
        void invalid_email() {
            email = "invalid";
            getUser("email");
        }
    }

    @Nested
    class change_me {
        String firstName;
        String lastName;

        Function<WebTestClient, WebTestClient.ResponseSpec> changeMe =
                client -> client.put()
                                .uri("/api/users/self")
                                .bodyValue(new ChangeUserDto(firstName, lastName))
                                .exchange();

        @BeforeEach
        void setUp() {
            firstName = "john";
            lastName = "smith";
        }

        @ParameterizedTest
        @EmptySource
        @MethodSource("com.samkruglov.base.validation.UserValidationTest#very_long_name_factory")
        void invalid_first_and_last_name(String invalidName) {
            firstName = invalidName;
            lastName = invalidName;
            sendAndAssertFields(changeMe, "firstName", "lastName");
        }

        @Test
        void missing_both_first_and_last_name() {
            firstName = null;
            lastName = null;
            sendAndAssertMessage(changeMe, "must not be null", "firstName", "lastName");
        }

        @Test
        void allowed_missing_first_name() {
            firstName = null;
            sendAndAssertValid(changeMe);
        }

        @Test
        void allowed_missing_last_name() {
            lastName = null;
            sendAndAssertValid(changeMe);
        }
    }

    @Nested
    class change_user {

        String firstName;
        String lastName;
        String email;

        Function<WebTestClient, WebTestClient.ResponseSpec> changeUser =
                client -> client.put()
                                .uri("/api/users/{email}", email)
                                .bodyValue(new ChangeUserDto(firstName, lastName))
                                .exchange();

        @BeforeEach
        void setUp() {
            firstName = "john";
            lastName = "smith";
            email = "john.smith@company.com";
        }

        @Test
        void invalid_email() {
            email = "invalid";
            sendAndAssertFields(changeUser, "email");
        }

        @ParameterizedTest
        @EmptySource
        @MethodSource("com.samkruglov.base.validation.UserValidationTest#very_long_name_factory")
        void invalid_first_and_last_name(String invalidName) {
            firstName = invalidName;
            lastName = invalidName;
            sendAndAssertFields(changeUser, "firstName", "lastName");
        }

        @Test
        void missing_both_first_and_last_name() {
            firstName = null;
            lastName = null;
            sendAndAssertMessage(changeUser, "must not be null", "firstName", "lastName");
        }

        @Test
        void allowed_missing_first_name() {
            firstName = null;
            sendAndAssertValid(changeUser);
        }

        @Test
        void allowed_missing_last_name() {
            lastName = null;
            sendAndAssertValid(changeUser);
        }
    }

    @Nested
    class remove_user {

        String email;

        @BeforeEach
        void setUp() {
            email = "john.smith@company.com";
        }

        void removeUser(String... expectedInvalidFields) {
            sendAndAssertFields(
                    client -> client.delete()
                                    .uri("/api/users/{email}", email)
                                    .exchange(),
                    expectedInvalidFields
            );
        }

        @Test
        void invalid_email() {
            email = "invalid";
            removeUser("email");
        }
    }

    static Stream<String> very_long_name_factory() {
        val builder = new StringBuilder();
        IntStream.range(0, 300).mapToObj(i -> "a").forEach(builder::append);
        return Stream.of(builder.toString());
    }
}
