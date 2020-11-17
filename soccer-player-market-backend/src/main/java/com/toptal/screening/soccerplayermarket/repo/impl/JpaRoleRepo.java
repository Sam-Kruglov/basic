package com.toptal.screening.soccerplayermarket.repo.impl;

import com.toptal.screening.soccerplayermarket.domain.Role;
import com.toptal.screening.soccerplayermarket.repo.RoleRepo;
import com.toptal.screening.soccerplayermarket.repo.config.HibernateRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaRoleRepo extends RoleRepo, HibernateRepository<Role, Long> {

    @Override
    default Role findByName(String name) {
        return findByNaturalId(name).orElseThrow(() -> new IllegalStateException("role " + name + " does not exist"));
    }
}
