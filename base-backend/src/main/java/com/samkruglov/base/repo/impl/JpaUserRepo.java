package com.samkruglov.base.repo.impl;

import com.samkruglov.base.domain.User;
import com.samkruglov.base.repo.UserRepo;
import com.samkruglov.base.repo.config.HibernateRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import javax.persistence.QueryHint;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Optional;

@Repository("userRepo")
public interface JpaUserRepo extends UserRepo, HibernateRepository<User, Long> {

    @Override
    default Optional<User> findByEmail(String email) {
        return findByNaturalId(email);
    }

    @Override
    default Optional<User> findReferenceByEmail(String email) {
        return findReferenceByNaturalId(email);
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
