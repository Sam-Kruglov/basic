package com.samkruglov.base.api;

import com.samkruglov.base.domain.User;
import com.samkruglov.base.service.UserService;
import com.samkruglov.base.api.view.CreateUserDto;
import com.samkruglov.base.api.view.GetUserDto;
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
    public void createUser(@RequestBody CreateUserDto userDto) {
        userService.create(userDto);
    }

    @GetMapping("/self")
    public GetUserDto getMe(@AuthenticationPrincipal(expression = "delegate") User user) {
        return new GetUserDto(user.getEmail());
    }

    @GetMapping("/{email}")
    public GetUserDto getUser(@PathVariable String email) {
        return new GetUserDto(userService.getByEmail(email).getEmail());
    }

    @DeleteMapping("/{email}")
    public void removeUser(@PathVariable String email) {
        userService.deleteByEmail(email);
    }

    @DeleteMapping("/self")
    public void removeMe(@AuthenticationPrincipal(expression = "delegate") User user) {
        userService.delete(user);
    }
}
