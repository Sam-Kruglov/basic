package com.samkruglov.base.validation;

import com.samkruglov.base.api.UserController;
import com.samkruglov.base.api.view.mapper.UserMapper;
import com.samkruglov.base.api.view.request.ChangeUserDto;
import com.samkruglov.base.api.view.request.CreateUserDto;
import com.samkruglov.base.config.ValidationTest;
import com.samkruglov.base.service.UserService;
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
import java.util.stream.Stream;

import static com.samkruglov.base.api.config.UserUrlPathId.BY_EMAIL;
import static com.samkruglov.base.api.config.UserUrlPathId.SELF;

@Import(UserController.class)
public class UserValidationTest extends ValidationTest {

    private static final String LONG_NAME = build_long_string(300);

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
        @ValueSource(strings = { "s", "invalid" })
        void invalid_email(String invalidEmail) {
            email = invalidEmail;
            createUser("email");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @MethodSource("com.samkruglov.base.validation.UserValidationTest#long_name_factory")
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
                    client -> client.get().uri("/api/users/" + BY_EMAIL, email).exchange(),
                    expectedInvalidFields
            );
        }

        @Test
        void invalid_email() {
            email = "invalid";
            getUser("email");
        }
    }

    abstract class abstract_change {
        protected String firstName;
        protected String lastName;

        protected Function<WebTestClient, WebTestClient.ResponseSpec> changeSender = buildSender();

        abstract Function<WebTestClient, WebTestClient.ResponseSpec> buildSender();

        protected Function<WebTestClient, WebTestClient.ResponseSpec> buildSender(String uri, Object... params) {
            return client -> client.put()
                                   .uri(uri, params)
                                   .bodyValue(new ChangeUserDto(firstName, lastName))
                                   .exchange();
        }

        @BeforeEach
        void setUp() {
            firstName = "john";
            lastName = "smith";
        }

        @ParameterizedTest
        @EmptySource
        @MethodSource("com.samkruglov.base.validation.UserValidationTest#long_name_factory")
        void invalid_first_and_last_name(String invalidName) {
            firstName = invalidName;
            lastName = invalidName;
            sendAndAssertFields(changeSender, "firstName", "lastName");
        }

        @Test
        void missing_all() {
            firstName = null;
            lastName = null;
            sendAndAssertMessage(changeSender, "must not be null", "firstName", "lastName");
        }

        @Test
        void allowed_missing_first_name() {
            firstName = null;
            sendAndAssertValid(changeSender);
        }

        @Test
        void allowed_missing_last_name() {
            lastName = null;
            sendAndAssertValid(changeSender);
        }
    }

    @Nested
    class change_me extends abstract_change {

        @Override
        Function<WebTestClient, WebTestClient.ResponseSpec> buildSender() {
            return buildSender("/api/users/" + SELF);
        }
    }

    @Nested
    class change_user extends abstract_change {

        @Override
        Function<WebTestClient, WebTestClient.ResponseSpec> buildSender() {
            return buildSender("/api/users/" + BY_EMAIL, "john.smith@company.com");
        }
    }

    static Stream<String> long_name_factory() {
        return Stream.of(LONG_NAME);
    }
}
