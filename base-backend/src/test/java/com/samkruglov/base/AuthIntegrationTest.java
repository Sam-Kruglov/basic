package com.samkruglov.base;

import com.samkruglov.base.client.gen.api.AuthApi;
import com.samkruglov.base.client.gen.view.ChangePasswordDto;
import com.samkruglov.base.config.IntegrationTest;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.samkruglov.base.config.TestUtil.assertThatUnauthorized;
import static org.assertj.core.api.Assertions.assertThatNoException;

@SuppressWarnings("Convert2MethodRef")
public class AuthIntegrationTest extends IntegrationTest {

    String email = "john.smith@company.com";
    String password = "js";
    AuthApi authApi;

    void authenticate() {
        apiClient.authenticate(email, password);
    }

    @BeforeAll
    void setUp() {
        authApi = apiClient.buildClient(AuthApi.class);
    }

    @Test
    void try_wrong_credentials__return_401() {
        assertThatUnauthorized().isThrownBy(() -> authenticate());
    }

    @Nested
    class given_user {

        @BeforeAll
        void setUp() {
            clearDatabase();
            userFactory.createUser(email, password);
        }

        @Test
        void login__success() {
            assertThatNoException().isThrownBy(() -> authenticate());
        }

        @Nested
        class given_authenticated {

            @BeforeAll
            void setUp() {
                authenticate();
            }

            @Test
            void change_password__success() {
                assertThatNoException().isThrownBy(() -> {
                    val newPassword = password + "1";
                    authApi.changePassword(new ChangePasswordDto().oldPassword(password).newPassword(newPassword));
                    password = newPassword;
                    authenticate();
                });
            }
        }
    }
}
