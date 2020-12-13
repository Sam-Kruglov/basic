package com.samkruglov.base.api;

import com.samkruglov.base.api.config.Current;
import com.samkruglov.base.api.config.Referred;
import com.samkruglov.base.api.view.mapper.UserMapper;
import com.samkruglov.base.api.view.request.ChangeUserDto;
import com.samkruglov.base.api.view.request.CreateUserDto;
import com.samkruglov.base.api.view.response.GetUserDto;
import com.samkruglov.base.domain.User;
import com.samkruglov.base.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static com.samkruglov.base.api.config.UserUrlPathId.BY_EMAIL;
import static com.samkruglov.base.api.config.UserUrlPathId.SELF;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "users")
public class UserController {

    public static final String CREATE_USER_OP_ID = "create-user";

    private final UserService service;
    private final UserMapper mapper;

    @Operation(operationId = CREATE_USER_OP_ID)
    @PostMapping
    public void createUser(@Valid @RequestBody CreateUserDto userDto) {
        service.create(userDto);
    }

    @GetMapping(SELF)
    public GetUserDto getMe(@Current User user) {
        return mapper.toGetUserDto(user);
    }

    @GetMapping(BY_EMAIL)
    public GetUserDto getUser(@Referred User user) {
        return mapper.toGetUserDto(user);
    }

    @PutMapping(SELF)
    public void changeMe(@Current User user, @Valid @RequestBody ChangeUserDto changeDto) {
        service.change(user, changeDto);
    }

    @PutMapping(BY_EMAIL)
    @PreAuthorize("not #user.hasRole(@roles.ADMIN)")
    public void changeUser(@Referred User user, @Valid @RequestBody ChangeUserDto changeDto) {
        service.change(user, changeDto);
    }

    @DeleteMapping(BY_EMAIL)
    @PreAuthorize("not #user.hasRole(@roles.ADMIN)")
    public void removeUser(@Referred User user) {
        service.delete(user);
    }

    @DeleteMapping(SELF)
    public void removeMe(@Current User user) {
        service.delete(user);
    }
}
