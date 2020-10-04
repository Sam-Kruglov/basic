package com.toptal.screening.soccerplayermarket.repo;

import com.toptal.screening.soccerplayermarket.domain.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends CrudRepository<User, Long> {

    @JpaCached
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
