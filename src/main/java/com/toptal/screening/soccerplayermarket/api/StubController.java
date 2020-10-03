package com.toptal.screening.soccerplayermarket.api;

import com.toptal.screening.soccerplayermarket.config.SecurityConfig;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StubController {

    private final UserDetailsManager userDetailsManager;
    private final PasswordEncoder passwordEncoder;

    @Value
    public static class UserDto {
        String username, password;
    }

    @PostMapping("/users")
    public void createUser(@RequestBody UserDto userDto) {
        userDetailsManager.createUser(User.builder()
                .username(userDto.getUsername())
                .passwordEncoder(passwordEncoder::encode)
                .password(userDto.getPassword())
                .roles(SecurityConfig.ROLE_USER)
                .build());
    }

    @GetMapping("/users/me")
    public String getMe(@AuthenticationPrincipal(expression = "username") String username) {
        return "you are " + username;
    }

    @GetMapping("/users/{username}")
    public String getUser(@PathVariable String username) {
        userDetailsManager.loadUserByUsername(username);
        return "found";
    }

    @DeleteMapping("/users/{username}")
    public String removeUser(@PathVariable String username) {
        userDetailsManager.deleteUser(username);
        return "removed";
    }

    @DeleteMapping("/users/me")
    public String removeMe(@AuthenticationPrincipal(expression = "username") String username) {
        userDetailsManager.deleteUser(username);
        return "removed";
    }

    @GetMapping("/users/me/teams")
    public void getTeams() {
    }
}
