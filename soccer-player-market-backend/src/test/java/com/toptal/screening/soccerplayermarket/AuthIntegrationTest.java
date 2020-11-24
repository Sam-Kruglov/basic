package com.toptal.screening.soccerplayermarket;

import com.toptal.screening.soccerplayermarket.config.IntegrationTest;
import com.toptal.screening.soccerplayermarket.config.UserTestFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;

import static com.toptal.screening.soccerplayermarket.config.TestUtil.assertThatUnauthorized;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@SuppressWarnings("Convert2MethodRef")
public class AuthIntegrationTest extends IntegrationTest {

    String email = "someone@gmail.com";
    String password = "123";

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
        void setUp(@Autowired UserTestFactory userFactory) {
            clearDatabase();
            userFactory.createUser(email, password);
        }

        @Test
        void success() {
            assertThatNoException().isThrownBy(() -> authenticate());
        }
    }
}
