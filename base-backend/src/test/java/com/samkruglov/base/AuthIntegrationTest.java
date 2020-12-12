package com.samkruglov.base;

import com.samkruglov.base.client.gen.api.AuthApi;
import com.samkruglov.base.client.gen.view.ChangePasswordDto;
import com.samkruglov.base.config.IntegrationTest;
import com.samkruglov.base.config.UserTestFactory;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.samkruglov.base.config.TestUtil.Client.assertThatUnauthorized;
import static org.assertj.core.api.Assertions.assertThatNoException;

@SuppressWarnings("Convert2MethodRef")
public class AuthIntegrationTest extends IntegrationTest {

    String  email = "john.smith@company.com";
    AuthApi authApi;

    void login() {
        login(email);
    }

    @BeforeAll
    void setUp() {
        authApi = apiClient.buildClient(AuthApi.class);
    }

    @Test
    void try_wrong_credentials__return_401() {
        assertThatUnauthorized().isThrownBy(() -> login());
    }

    @Nested
    class given_user {

        @BeforeAll
        void setUp() {
            clearDatabase();
            userFactory.saveUser(email);
        }

        @Test
        void login__success() {
            assertThatNoException().isThrownBy(() -> login());
        }

        @Nested
        class given_authenticated {

            @BeforeAll
            void setUp() {
                login();
            }

            @Test
            void change_password__success() {
                assertThatNoException().isThrownBy(() -> {
                    val oldPassword = UserTestFactory.PASSWORD;
                    val newPassword = oldPassword + "1";
                    authApi.changePassword(new ChangePasswordDto().oldPassword(oldPassword).newPassword(newPassword));
                    apiClient.authenticate(email, newPassword);
                });
            }
        }
    }
}
