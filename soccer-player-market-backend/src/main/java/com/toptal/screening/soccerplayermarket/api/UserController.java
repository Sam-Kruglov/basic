package com.toptal.screening.soccerplayermarket.api;

import com.toptal.screening.soccerplayermarket.api.view.UserDto;
import com.toptal.screening.soccerplayermarket.domain.User;
import com.toptal.screening.soccerplayermarket.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "users")
public class UserController {

    public static final String CREATE_USER_OP_ID = "create-user";

    private final UserService userService;

    @Operation(operationId = CREATE_USER_OP_ID)
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
    public String removeMe(@AuthenticationPrincipal(expression = "delegate") User user) {
        userService.delete(user);
        return "removed";
    }
}
