package com.samkruglov.base;

import com.samkruglov.base.config.IntegrationTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static com.samkruglov.base.config.TestUtil.assertThatUnauthorized;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@SuppressWarnings("Convert2MethodRef")
public class AuthIntegrationTest extends IntegrationTest {

    String email = "john.smith@company.com";
    String password = "js";

    void authenticate() {
        apiClient.authenticate(email, password);
    }

    @Test
    void try_wrong_credentials__return_401() {
        assertThatUnauthorized().isThrownBy(() -> authenticate());
    }

    @Nested
    @TestInstance(PER_CLASS)
    class given_user {

        @BeforeAll
        void setUp() {
            clearDatabase();
            userFactory.createUser(email, password);
        }

        @Test
        void success() {
            assertThatNoException().isThrownBy(() -> authenticate());
        }
    }
}
