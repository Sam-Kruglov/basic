package com.samkruglov.base;

import com.samkruglov.base.client.gen.view.CreateUserDto;
import com.samkruglov.base.config.IntegrationTest;
import com.samkruglov.base.config.UserTestFactory;
import com.samkruglov.base.client.gen.api.UsersApi;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;

import static com.samkruglov.base.config.TestUtil.assertNoPermissionTo;
import static com.samkruglov.base.config.TestUtil.assertThatUnauthorized;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

class UserIntegrationTest extends IntegrationTest {

    UsersApi usersApi;

    String email = "john@company.com";
    String password = "jn";

    void login() {
        apiClient.authenticate(email, password);
    }

    @BeforeAll
    void setUp() {
        usersApi = apiClient.buildClient(UsersApi.class);
    }

    @Nested
    @TestMethodOrder(OrderAnnotation.class)
    class create_user_flow {

        @BeforeAll
        void setUp() {
            clearDatabase();
        }

        @Order(1)
        @Test
        void create_user_and_login() {
            val createUserDto = new CreateUserDto().email(email).password(password);
            usersApi.createUser(createUserDto);
            login();
        }

        @Order(2)
        @Test
        void find_self__found() {
            assertThat(usersApi.getMe().getEmail()).isEqualTo(email);
        }

        @Order(3)
        @Test
        void remove_self() {
            assertThatNoException().isThrownBy(() -> usersApi.removeMe());
        }

        @Order(4)
        @Test
        void find_self__logged_out() {
            assertThatUnauthorized().isThrownBy(() -> usersApi.getMe());
        }
    }

    @Nested
    class given_authenticated_user {

        @BeforeAll
        void setUp(@Autowired UserTestFactory userFactory) {
            clearDatabase();
            userFactory.createUser(email, password);
            login();
        }

        @Test
        void cannot_create_more_users() {
            assertNoPermissionTo(() -> usersApi.createUser(new CreateUserDto().email("em").password("p")));
        }

        @Nested
        class given_another_user_2 {
            String email2 = "mike@company.com";
            String password2 = "mk";

            @BeforeAll
            void setUp(@Autowired UserTestFactory userFactory) {
                userFactory.createUser(email2, password2);
            }

            @Test
            void cannot_see_user_2() {
                assertNoPermissionTo(() -> usersApi.getUser(email2));
            }

            @Test
            void cannot_remove_user_2() {
                assertNoPermissionTo(() -> usersApi.removeUser(email2));
            }
        }
    }

    @Nested
    class given_authenticated_admin {

        @BeforeAll
        void setUp(@Autowired UserTestFactory userFactory) {
            clearDatabase();
            userFactory.createAdmin(email, password);
            login();
        }

        @Test
        void can_create_more_users() {
            assertThatNoException()
                    .isThrownBy(() -> usersApi.createUser(new CreateUserDto().email("em").password("p")));
        }

        @Test
        void cannot_delete_self() {
            assertNoPermissionTo(() -> usersApi.removeMe());
        }

        @Nested
        @TestMethodOrder(OrderAnnotation.class)
        class given_another_user_2 {
            String email2 = "rob@company.com";
            String password2 = "rb";

            @BeforeAll
            void setUp(@Autowired UserTestFactory userFactory) {
                userFactory.createUser(email2, password2);
            }

            @Order(1)
            @Test
            void can_see_user_2() {
                assertThat(usersApi.getUser(email2).getEmail()).isEqualTo(email2);
            }

            @Order(2)
            @Test
            void can_remove_user_2() {
                assertThatNoException().isThrownBy(() -> usersApi.removeUser(email2));
            }
        }

        @Nested
        @TestMethodOrder(OrderAnnotation.class)
        class given_another_admin_2 {
            String email2 = "mike@company.com";
            String password2 = "mk";

            @BeforeAll
            void setUp(@Autowired UserTestFactory userFactory) {
                userFactory.createAdmin(email2, password2);
            }

            @Order(1)
            @Test
            void can_see_admin_2() {
                assertThat(usersApi.getUser(email2).getEmail()).isEqualTo(email2);
            }

            @Order(2)
            @Test
            void cannot_remove_admin_2() {
                assertNoPermissionTo(() -> usersApi.removeUser(email2));
            }
        }
    }
}
