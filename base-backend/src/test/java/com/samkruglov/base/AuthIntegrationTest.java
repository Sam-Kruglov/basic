package com.samkruglov.base;

import com.samkruglov.base.client.gen.api.AuthApi;
import com.samkruglov.base.client.gen.view.ChangePasswordDto;
import com.samkruglov.base.client.gen.view.ChangeUserPasswordDto;
import com.samkruglov.base.config.IntegrationTest;
import com.samkruglov.base.config.UserTestFactory;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static com.samkruglov.base.config.TestUtil.Client.assertNoPermissionTo;
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
        @TestMethodOrder(OrderAnnotation.class)
        class given_authenticated {

            String newPassword;

            @BeforeAll
            void setUp() {
                login();
            }

            @Test
            @Order(1)
            void change_password__success() {
                val oldPassword = UserTestFactory.PASSWORD;
                newPassword = oldPassword + "1";
                assertThatNoException().isThrownBy(() -> {
                    authApi.changeMyPassword(new ChangePasswordDto().oldPassword(oldPassword).newPassword(newPassword));
                    apiClient.authenticate(email, newPassword);
                });
            }

            @Test
            @Order(2)
            void login_with_new_password__success() {
                assertThatNoException().isThrownBy(() -> apiClient.authenticate(email, newPassword));
            }

            @Nested
            class given_another_user_2 {
                String email2 = "mike.gordon@company.com";

                @BeforeAll
                void setUp() {
                    userFactory.saveUser(email2);
                }

                @Test
                void cannot_change_password_of_user_2() {
                    val newPassword = UserTestFactory.PASSWORD + "1";
                    assertNoPermissionTo(() -> authApi.changeUserPassword(
                            email2,
                            new ChangeUserPasswordDto().newPassword(newPassword)
                    ));
                }
            }
        }
    }

    @Nested
    class given_authenticated_admin {

        @BeforeAll
        void setUp() {
            clearDatabase();
            userFactory.saveAdmin(email);
            login();
        }


        @Nested
        @TestMethodOrder(OrderAnnotation.class)
        class given_another_user_2 {
            String email2 = "rob.wilson@company.com";
            String newPassword;

            @BeforeAll
            void setUp() {
                userFactory.saveUser(email2);
            }

            @Test
            @Order(1)
            void can_change_password_of_user_2() {
                newPassword = UserTestFactory.PASSWORD + "1";
                assertThatNoException().isThrownBy(() -> authApi.changeUserPassword(
                        email2,
                        new ChangeUserPasswordDto().newPassword(newPassword)
                ));
            }

            @Test
            @Order(2)
            void user_2_can_login_with_new_password() {
                assertThatNoException().isThrownBy(() -> apiClient.authenticate(email2, newPassword));
            }
        }

        @Nested
        class given_another_admin_2 {
            String email2 = "mike.gordon@company.com";

            @BeforeAll
            void setUp() {
                userFactory.saveAdmin(email2);
            }

            @Test
            void cannot_change_password_of_admin_2() {
                val newPassword = UserTestFactory.PASSWORD + "1";
                assertNoPermissionTo(() -> authApi.changeUserPassword(
                        email2,
                        new ChangeUserPasswordDto().newPassword(newPassword)
                ));
            }
        }
    }
}
