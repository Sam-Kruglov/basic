package com.samkruglov.base.service;

import com.samkruglov.base.api.view.ChangeUserDto;
import com.samkruglov.base.api.view.CreateUserDto;
import com.samkruglov.base.api.view.mapper.UserMapper;
import com.samkruglov.base.config.Roles;
import com.samkruglov.base.domain.User;
import com.samkruglov.base.repo.RoleRepo;
import com.samkruglov.base.repo.UserRepo;
import com.samkruglov.base.service.error.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.samkruglov.base.service.error.BaseErrorType.EMAIL_ALREADY_EXISTS;
import static com.samkruglov.base.service.error.BaseErrorType.USER_NOT_FOUND;

/**
 * @implNote there is an abstraction {@link UserDetailsManager} that may be useful in the future Spring releases.
 * It offers no JPA support and it doesn't seem to bring any value, so, we're ignoring it for now.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepo repo;
    private final UserMapper mapper;
    private final RoleRepo roleRepo;

    public void create(CreateUserDto dto) {
        if (repo.existsByEmail(dto.getEmail())) {
            throw new BaseException(EMAIL_ALREADY_EXISTS);
        }
        val userRole = roleRepo.findByName(Roles.USER);
        repo.save(mapper.toUser(dto, List.of(userRole)));
    }

    public User getByEmail(String email) {
        return repo.findByEmail(email).orElseThrow(() -> new BaseException(USER_NOT_FOUND));
    }

    public void deleteByEmail(String email) {
        delete(getByEmail(email));
    }

    public void delete(User user) {
        repo.delete(user);
    }

    public void change(User user, ChangeUserDto changeDto) {
        mapper.updateUser(user, changeDto);
        repo.save(user);
    }

    public void changeByEmail(String email, ChangeUserDto changeDto) {
        change(getByEmail(email), changeDto);
    }
}
