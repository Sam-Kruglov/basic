package com.toptal.screening.soccerplayermarket.repo.impl;

import com.toptal.screening.soccerplayermarket.domain.User;
import com.toptal.screening.soccerplayermarket.repo.UserRepo;
import com.toptal.screening.soccerplayermarket.repo.config.HibernateRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import javax.persistence.QueryHint;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Optional;

@Repository
public interface JpaUserRepo extends UserRepo, HibernateRepository<User, Long> {

    @Cached
    @Override
    Optional<User> findById(Long id);

    @Override
    default Optional<User> findByEmail(String email) {
        return findByNaturalId(email);
    }

    @Override
    boolean existsByEmail(String email);

    @QueryHints({
            @QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"),
            @QueryHint(name = org.hibernate.annotations.QueryHints.CACHE_REGION, value = User.TABLE_NAME)
    })
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Cached {}
}
