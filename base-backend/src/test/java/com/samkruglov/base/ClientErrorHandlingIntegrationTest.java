package com.samkruglov.base;

import com.samkruglov.base.client.gen.api.UsersApi;
import com.samkruglov.base.config.IntegrationTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.samkruglov.base.client.gen.view.ErrorResponse.CodeEnum.INVALID_REQUEST;
import static com.samkruglov.base.client.gen.view.ErrorResponse.CodeEnum.USER_NOT_FOUND;
import static com.samkruglov.base.config.TestUtil.Client.assertThatBaseException;

public class ClientErrorHandlingIntegrationTest extends IntegrationTest {

    @Test
    void resolves_validation_error() {
        assertThatBaseException(INVALID_REQUEST)
                .isThrownBy(() -> apiClient.authenticate("invalid", ""))
                .withMessageContaining("email")
                .withMessageContaining("password");
    }

    @Nested
    class given_authenticated_admin {

        String   email    = "john.smith@company.com";
        UsersApi usersApi;

        @BeforeAll
        void setUp() {
            userFactory.saveAdmin(email);
            login(email);
            usersApi = apiClient.buildClient(UsersApi.class);
        }

        @Test
        void given_searching_for_non_existing_user__resolves_not_found_error() {
            assertThatBaseException(USER_NOT_FOUND)
                    .isThrownBy(() -> usersApi.getUser("non.existent.user@company.com"))
                    .withMessageContaining("not found");
        }
    }
}
