package com.toptal.screening.soccerplayermarket.repo;

import com.toptal.screening.soccerplayermarket.domain.Role;
import org.springframework.data.repository.Repository;

@org.springframework.stereotype.Repository
public interface RoleRepo extends Repository<Role, Long> {

    @JpaCached
    Role findByName(String name);
}
