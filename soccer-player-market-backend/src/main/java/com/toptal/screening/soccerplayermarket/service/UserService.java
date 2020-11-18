package com.toptal.screening.soccerplayermarket.service;

import com.toptal.screening.soccerplayermarket.api.view.CreateUserDto;
import com.toptal.screening.soccerplayermarket.config.Roles;
import com.toptal.screening.soccerplayermarket.domain.User;
import com.toptal.screening.soccerplayermarket.repo.RoleRepo;
import com.toptal.screening.soccerplayermarket.repo.UserRepo;
import com.toptal.screening.soccerplayermarket.service.error.SoccerMarketException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.toptal.screening.soccerplayermarket.service.error.SoccerMarketErrorType.EMAIL_ALREADY_EXISTS;
import static com.toptal.screening.soccerplayermarket.service.error.SoccerMarketErrorType.USER_NOT_FOUND;

/**
 * @implNote there is an abstraction {@link UserDetailsManager} that may be useful in the future Spring releases.
 * It offers no JPA support and it doesn't seem to bring any value, so, we're ignoring it for now.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepo repo;
    private final RoleRepo roleRepo;
    private final PasswordEncoder passwordEncoder;

    public void create(CreateUserDto dto) {
        if (repo.existsByEmail(dto.getEmail())) {
            throw new SoccerMarketException(EMAIL_ALREADY_EXISTS);
        }
        val userRole = roleRepo.findByName(Roles.USER);
        repo.save(new User(dto.getEmail(), passwordEncoder.encode(dto.getPassword()), List.of(userRole)));
    }

    public User getByEmail(String email) {
        return repo.findByEmail(email).orElseThrow(() -> new SoccerMarketException(USER_NOT_FOUND));
    }

    public void deleteByEmail(String email) {
        delete(getByEmail(email));
    }

    public void delete(User user) {
        repo.delete(user);
    }
}
