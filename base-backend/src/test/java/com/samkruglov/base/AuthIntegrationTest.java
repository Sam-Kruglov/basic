package com.samkruglov.base;

import com.samkruglov.base.client.gen.api.AuthApi;
import com.samkruglov.base.client.gen.view.ChangePasswordDto;
import com.samkruglov.base.config.IntegrationTest;
import com.samkruglov.base.config.UserTestFactory;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

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
        clearDatabase();
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
        @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
        class given_authenticated {

            String newPassword;

            @BeforeAll
            void setUp() {
                login();
            }

            @Test
            @Order(1)
            void change_password__success() {
                assertThatNoException().isThrownBy(() -> {
                    val oldPassword = UserTestFactory.PASSWORD;
                    newPassword = oldPassword + "1";
                    authApi.changeMyPassword(new ChangePasswordDto().oldPassword(oldPassword).newPassword(newPassword));
                    apiClient.authenticate(email, newPassword);
                });
            }

            @Test
            @Order(2)
            void login_with_new_password__success() {
                assertThatNoException().isThrownBy(() -> apiClient.authenticate(email, newPassword));
            }
        }
    }
}
