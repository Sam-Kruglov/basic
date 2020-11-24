package com.samkruglov.base.repo.impl;

import com.samkruglov.base.repo.config.HibernateRepository;
import com.samkruglov.base.domain.Role;
import com.samkruglov.base.repo.RoleRepo;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaRoleRepo extends RoleRepo, HibernateRepository<Role, Long> {

    @Override
    default Role findByName(String name) {
        return findByNaturalId(name).orElseThrow(() -> new IllegalStateException("role " + name + " does not exist"));
    }
}
