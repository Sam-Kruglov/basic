package com.samkruglov.base.validation;

import com.samkruglov.base.api.AuthController;
import com.samkruglov.base.api.view.request.ChangePasswordDto;
import com.samkruglov.base.api.view.request.CredentialsDto;
import com.samkruglov.base.config.ValidationTest;
import com.samkruglov.base.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

@Import(AuthController.class)
public class AuthValidationTest extends ValidationTest {

    @MockBean AuthService service;

    @Nested
    class login {

        String email;
        String password;

        @BeforeEach
        void setUp() {
            email = "john.smith@company.com";
            password = "password";
        }

        void login(String... expectedInvalidFields) {
            sendAndAssertFields(
                    client -> client.post()
                                    .uri("/api/auth/login")
                                    .bodyValue(new CredentialsDto(email, password))
                                    .exchange(),
                    expectedInvalidFields
            );
        }

        /**
         * For more examples see https://github.com/hibernate/hibernate-validator/blob/master/engine/src/test/java/org/hibernate/validator/test/internal/constraintvalidators/hv/EmailValidatorTest.java
         */
        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"john", "john.company", "john.company.com"})
        void invalid_email(String invalidEmail) {
            email = invalidEmail;
            login("email");
        }

        @ParameterizedTest
        @NullAndEmptySource
        void invalid_password(String invalidPassword) {
            password = invalidPassword;
            login("password");
        }
    }

    @Nested
    class change_password {

        String oldPassword;
        String newPassword;

        @BeforeEach
        void setUp() {
            oldPassword = "oldPassword";
            newPassword = "password";
        }

        void changePassword(String... expectedInvalidFields) {
            sendAndAssertFields(
                    client -> client.put()
                                    .uri("/api/auth/users/self/change-password")
                                    .bodyValue(new ChangePasswordDto(oldPassword, newPassword))
                                    .exchange(),
                    expectedInvalidFields
            );
        }

        @ParameterizedTest
        @NullAndEmptySource
        void invalid_old_password(String invalidPassword) {
            oldPassword = invalidPassword;
            changePassword("oldPassword");
        }

        @ParameterizedTest
        @NullAndEmptySource
        void invalid_new_password(String invalidPassword) {
            newPassword = invalidPassword;
            changePassword("newPassword");
        }
    }
}
