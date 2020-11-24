package com.samkruglov.base.repo;

import com.samkruglov.base.domain.Role;

public interface RoleRepo {
    Role findByName(String name);
}
