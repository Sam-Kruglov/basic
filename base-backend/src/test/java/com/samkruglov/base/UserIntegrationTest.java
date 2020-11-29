package com.samkruglov.base;

import com.samkruglov.base.client.gen.api.UsersApi;
import com.samkruglov.base.client.gen.view.ChangeUserDto;
import com.samkruglov.base.client.gen.view.CreateUserDto;
import com.samkruglov.base.config.IntegrationTest;
import lombok.val;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.samkruglov.base.config.TestUtil.assertNoPermissionTo;
import static com.samkruglov.base.config.TestUtil.assertThatUnauthorized;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

class UserIntegrationTest extends IntegrationTest {

    UsersApi usersApi;

    String email = "john.smith@company.com";
    String password = "js";

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
            val createUserDto = new CreateUserDto()
                    .email(email)
                    .password(password)
                    .firstName("john")
                    .lastName("smith");
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
    @ExtendWith(SoftAssertionsExtension.class)
    class given_authenticated_user {

        @BeforeAll
        void setUp() {
            clearDatabase();
            userFactory.createUser(email, password);
            login();
        }

        @Test
        void cannot_create_more_users() {
            assertNoPermissionTo(() -> usersApi.createUser(new CreateUserDto().email("em").password("p")));
        }

        @Test
        void can_edit_self(SoftAssertions softly) {
            val newName = "steve";
            softly.assertThatCode(() -> usersApi.changeMe(new ChangeUserDto().firstName(newName)))
                  .doesNotThrowAnyException();
            softly.assertThat(usersApi.getMe().getFirstName()).isEqualTo(newName);
        }

        @Nested
        class given_another_user_2 {
            String email2 = "mike.gordon@company.com";
            String password2 = "mg";

            @BeforeAll
            void setUp() {
                userFactory.createUser(email2, password2);
            }

            @Test
            void cannot_see_user_2() {
                assertNoPermissionTo(() -> usersApi.getUser(email2));
            }

            @Test
            void cannot_edit_user_2() {
                assertNoPermissionTo(() -> usersApi.changeUser(email2, new ChangeUserDto().firstName("steve")));
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
        void setUp() {
            clearDatabase();
            userFactory.createAdmin(email, password);
            login();
        }

        @Test
        void can_create_more_users() {
            val userDto = new CreateUserDto()
                    .firstName("mark")
                    .lastName("gold")
                    .email("mark.gold@company.com")
                    .password("p");
            assertThatNoException()
                    .isThrownBy(() -> usersApi.createUser(userDto));
        }

        @Test
        void cannot_delete_self() {
            assertNoPermissionTo(() -> usersApi.removeMe());
        }

        @Nested
        @TestMethodOrder(OrderAnnotation.class)
        class given_another_user_2 {
            String email2 = "rob.wilson@company.com";
            String password2 = "rw";

            @BeforeAll
            void setUp() {
                userFactory.createUser(email2, password2);
            }

            @Order(1)
            @Test
            void can_see_user_2() {
                assertThat(usersApi.getUser(email2).getEmail()).isEqualTo(email2);
            }

            @Order(2)
            @Test
            void can_edit_user_2() {
                assertThatNoException()
                        .isThrownBy(() -> usersApi.changeUser(email2, new ChangeUserDto().firstName("steve")));
            }

            @Order(3)
            @Test
            void can_remove_user_2() {
                assertThatNoException().isThrownBy(() -> usersApi.removeUser(email2));
            }
        }

        @Nested
        @TestMethodOrder(OrderAnnotation.class)
        class given_another_admin_2 {
            String email2 = "mike.gordon@company.com";
            String password2 = "mg";

            @BeforeAll
            void setUp() {
                userFactory.createAdmin(email2, password2);
            }

            @Order(1)
            @Test
            void can_see_admin_2() {
                assertThat(usersApi.getUser(email2).getEmail()).isEqualTo(email2);
            }

            @Order(2)
            @Test
            void cannot_edit_admin_2() {
                assertNoPermissionTo(() -> usersApi.changeUser(email2, new ChangeUserDto().firstName("steve")));
            }

            @Order(3)
            @Test
            void cannot_remove_admin_2() {
                assertNoPermissionTo(() -> usersApi.removeUser(email2));
            }
        }
    }
}
