package com.samkruglov.base.config;

import com.samkruglov.base.domain.User;
import com.samkruglov.base.repo.RoleRepo;
import com.samkruglov.base.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class UserTestFactory {
    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final PasswordEncoder passwordEncoder;

    public User createAdmin(String email, String password) {
        return create(email, password, Roles.USER, Roles.ADMIN);
    }

    public User createUser(String email, String password) {
        return create(email, password, Roles.USER);
    }

    public User create(String email, String password, String... roleNames) {
        val roles = Stream.of(roleNames).map(roleRepo::findByName).collect(toList());
        val user = new User(email, passwordEncoder.encode(password), roles);
        userRepo.save(user);
        return user;
    }
}
