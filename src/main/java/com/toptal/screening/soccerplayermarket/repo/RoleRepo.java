package com.toptal.screening.soccerplayermarket.repo;

import com.toptal.screening.soccerplayermarket.domain.Role;

public interface RoleRepo {
    Role findByName(String name);
}
