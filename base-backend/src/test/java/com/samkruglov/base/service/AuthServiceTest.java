package com.samkruglov.base.service;

import com.samkruglov.base.api.view.request.ChangePasswordDto;
import com.samkruglov.base.config.UserTestFactory;
import com.samkruglov.base.domain.User;
import com.samkruglov.base.repo.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static com.samkruglov.base.config.TestUtil.assertThatBaseException;
import static com.samkruglov.base.config.TestUtil.withProperty;
import static com.samkruglov.base.service.error.BaseErrorType.OLD_PASSWORD_DOES_NOT_MATCH;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("Convert2MethodRef")
class AuthServiceTest {

    @SuppressWarnings("deprecation")
    PasswordEncoder passwordEncoder = NoOpPasswordEncoder.getInstance();
    @Mock UserRepo userRepo;

    AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(null, null, userRepo, passwordEncoder, null);
    }

    @Nested
    class change_password {
        String oldPassword;
        String newPassword;
        User   user;

        @BeforeEach
        void setUp() {
            oldPassword = UserTestFactory.PASSWORD;
            newPassword = oldPassword + "1";
            user = UserTestFactory.createUser("john.smith@company.com");
        }

        void changePassword() {
            service.changePassword(user, new ChangePasswordDto(oldPassword, newPassword));
        }

        @Test
        void password_is_different() {
            changePassword();
            verify(userRepo).save(withProperty("encodedPassword", passwordEncoder.encode(newPassword)));
        }

        @Test
        void given_wrong_old_password__reject() {
            oldPassword += "wrong";

            assertThatBaseException(OLD_PASSWORD_DOES_NOT_MATCH).isThrownBy(() -> changePassword());

            verifyNoInteractions(userRepo);
        }
    }
}