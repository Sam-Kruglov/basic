package com.samkruglov.base.repo;

import com.samkruglov.base.domain.User;
import com.samkruglov.base.service.error.BaseException;

import java.util.Optional;

import static com.samkruglov.base.service.error.BaseErrorType.USER_NOT_FOUND;

public interface UserRepo {

    Optional<User> findByEmail(String email);

    default User getByEmail(String email) {
        return findByEmail(email).orElseThrow(() -> new BaseException(USER_NOT_FOUND));
    }

    boolean existsByEmail(String email);

    void save(User user);

    void delete(User user);
}
