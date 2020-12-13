package com.samkruglov.base.repo;

import com.samkruglov.base.domain.User;

import java.util.Optional;

public interface UserRepo {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    void save(User user);

    void delete(User user);
}
