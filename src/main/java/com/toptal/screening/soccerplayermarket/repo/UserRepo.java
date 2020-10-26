package com.toptal.screening.soccerplayermarket.repo;

import com.toptal.screening.soccerplayermarket.domain.User;

import java.util.Optional;

public interface UserRepo {

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    void save(User user);

    void delete(User user);
}
