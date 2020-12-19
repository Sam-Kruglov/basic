package com.samkruglov.base.config;

import com.samkruglov.base.domain.Role;
import com.samkruglov.base.domain.User;
import com.samkruglov.base.repo.RoleRepo;
import com.samkruglov.base.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.capitalize;

@Service
@RequiredArgsConstructor
public class UserTestFactory {

    public static final String PASSWORD = "password";

    private static final Pattern TEST_EMAIL_REGEX = Pattern.compile("(?<firstName>\\w+)\\.(?<lastName>\\w+)@.*");

    public static User createAdmin(String email) {
        return create(email, PASSWORD, Stream.of(new Role(1, Roles.USER), new Role(2, Roles.ADMIN)));
    }

    public static User createUser(String email) {
        return create(email, PASSWORD, Stream.of(new Role(1, Roles.USER)));
    }

    private static User create(String email, String password, Stream<Role> roles) {
        val matcher = TEST_EMAIL_REGEX.matcher(email);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(email + " doesn't match " + matcher);
        }
        return new User(
                capitalize(matcher.group("firstName")),
                capitalize(matcher.group("lastName")),
                email,
                password,
                roles.collect(toSet())
        );
    }

    private final UserRepo        userRepo;
    private final RoleRepo        roleRepo;
    private final PasswordEncoder passwordEncoder;

    public User saveAdmin(String email) {
        return save(email, Roles.USER, Roles.ADMIN);
    }

    public User saveUser(String email) {
        return save(email, Roles.USER);
    }

    private User save(String email, String... roleNames) {
        val user = create(
                email,
                passwordEncoder.encode(PASSWORD),
                Stream.of(roleNames).map(roleRepo::findByName)
        );
        userRepo.save(user);
        return user;
    }
}
