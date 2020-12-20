package com.samkruglov.base;

import com.samkruglov.base.client.gen.api.UsersApi;
import com.samkruglov.base.client.gen.view.CreateUserDto;
import com.samkruglov.base.client.gen.view.UpdateUserDto;
import com.samkruglov.base.config.IntegrationTest;
import com.samkruglov.base.config.UserTestFactory;
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

import static com.samkruglov.base.client.gen.view.ErrorResponse.CodeEnum.USER_NOT_FOUND;
import static com.samkruglov.base.config.TestUtil.Client.assertNoPermissionTo;
import static com.samkruglov.base.config.TestUtil.Client.assertThatBaseException;
import static com.samkruglov.base.config.TestUtil.Client.assertThatUnauthorized;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

class UserIntegrationTest extends IntegrationTest {

    UsersApi usersApi;

    String email = "john.smith@company.com";

    void login() {
        login(email);
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
                    .password(UserTestFactory.PASSWORD)
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
            userFactory.saveUser(email);
            login();
        }

        @Test
        void cannot_create_more_users() {
            assertNoPermissionTo(() -> usersApi.createUser(new CreateUserDto().email("em").password("p")));
        }

        @Test
        void can_edit_self(SoftAssertions softly) {
            val newName = "steve";
            softly.assertThatCode(() -> usersApi.updateMe(new UpdateUserDto().firstName(newName)))
                  .doesNotThrowAnyException();
            softly.assertThat(usersApi.getMe().getFirstName()).isEqualTo(newName);
        }

        @Nested
        class given_another_user_2 {
            String email2 = "mike.gordon@company.com";

            @BeforeAll
            void setUp() {
                userFactory.saveUser(email2);
            }

            @Test
            void cannot_see_user_2() {
                assertNoPermissionTo(() -> usersApi.getUser(email2));
            }

            @Test
            void cannot_edit_user_2() {
                assertNoPermissionTo(() -> usersApi.updateUser(email2, new UpdateUserDto().firstName("steve")));
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
            userFactory.saveAdmin(email);
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

            @BeforeAll
            void setUp() {
                userFactory.saveUser(email2);
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
                        .isThrownBy(() -> usersApi.updateUser(email2, new UpdateUserDto().firstName("steve")));
            }

            @Order(3)
            @Test
            void can_remove_user_2() {
                assertThatNoException().isThrownBy(() -> usersApi.removeUser(email2));
            }

            @Order(4)
            @Test
            void user_2_not_found() {
                assertThatBaseException(USER_NOT_FOUND)
                        .isThrownBy(() -> usersApi.getUser(email2));
            }
        }

        @Nested
        @TestMethodOrder(OrderAnnotation.class)
        class given_another_admin_2 {
            String email2 = "mike.gordon@company.com";

            @BeforeAll
            void setUp() {
                userFactory.saveAdmin(email2);
            }

            @Order(1)
            @Test
            void can_see_admin_2() {
                assertThat(usersApi.getUser(email2).getEmail()).isEqualTo(email2);
            }

            @Order(2)
            @Test
            void cannot_edit_admin_2() {
                assertNoPermissionTo(() -> usersApi.updateUser(email2, new UpdateUserDto().firstName("steve")));
            }

            @Order(3)
            @Test
            void cannot_remove_admin_2() {
                assertNoPermissionTo(() -> usersApi.removeUser(email2));
            }
        }
    }
}
