package com.samkruglov.base.repo;

import com.samkruglov.base.domain.User;
import com.samkruglov.base.service.error.BaseException;

import java.util.Optional;

import static com.samkruglov.base.service.error.BaseErrorType.USER_NOT_FOUND;

public interface UserRepo {

    Optional<User> findByEmail(String email);

    default User getByEmail(String email) {
        return unwrap(findByEmail(email));
    }

    Optional<User> findReferenceByEmail(String email);

    default User getReferenceByEmail(String email) {
        return unwrap(findReferenceByEmail(email));
    }

    boolean existsByEmail(String email);

    void save(User user);

    void delete(User user);

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    static User unwrap(Optional<User> userOpt) {
        return userOpt.orElseThrow(() -> new BaseException(USER_NOT_FOUND));
    }
}
