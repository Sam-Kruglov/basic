package com.toptal.screening.soccerplayermarket.api;

import com.toptal.screening.soccerplayermarket.api.view.UserDto;
import com.toptal.screening.soccerplayermarket.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public void createUser(@RequestBody UserDto userDto) {
        userService.create(userDto);
    }

    @GetMapping("/self")
    public String getMe(@AuthenticationPrincipal(expression = "delegate.email") String email) {
        return "you are " + email;
    }

    @GetMapping("/{email}")
    public String getUser(@PathVariable String email) {
        userService.getByEmail(email);
        return "found";
    }

    @DeleteMapping("/{email}")
    public String removeUser(@PathVariable String email) {
        userService.deleteByEmail(email);
        return "removed";
    }

    @DeleteMapping("/self")
    public String removeMe(@AuthenticationPrincipal(expression = "delegate.email") String email) {
        userService.deleteByEmail(email);
        return "removed";
    }
}
