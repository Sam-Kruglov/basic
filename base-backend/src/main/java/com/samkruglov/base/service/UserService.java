package com.samkruglov.base.service;

import com.samkruglov.base.api.view.mapper.UserMapper;
import com.samkruglov.base.api.view.request.ChangeUserDto;
import com.samkruglov.base.api.view.request.CreateUserDto;
import com.samkruglov.base.config.Roles;
import com.samkruglov.base.domain.User;
import com.samkruglov.base.repo.RoleRepo;
import com.samkruglov.base.repo.UserRepo;
import com.samkruglov.base.service.error.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;

import java.util.Set;

import static com.samkruglov.base.service.error.BaseErrorType.EMAIL_ALREADY_EXISTS;

/**
 * @implNote there is an abstraction {@link UserDetailsManager} that may be useful in the future Spring releases.
 * It offers no JPA support and it doesn't seem to bring any value, so, we're ignoring it for now.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepo   repo;
    private final UserMapper mapper;
    private final RoleRepo   roleRepo;

    public void create(CreateUserDto dto) {
        if (repo.existsByEmail(dto.getEmail())) {
            throw new BaseException(EMAIL_ALREADY_EXISTS);
        }
        val userRole = roleRepo.findByName(Roles.USER);
        repo.save(mapper.toUser(dto, Set.of(userRole)));
    }

    public void delete(User user) {
        repo.delete(user);
    }

    public void change(User user, ChangeUserDto changeDto) {
        mapper.updateUser(user, changeDto);
        repo.save(user);
    }
}
