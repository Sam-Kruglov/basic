package com.samkruglov.base.api.view.mapper;

import com.samkruglov.base.api.view.CreateUserDto;
import com.samkruglov.base.api.view.GetUserDto;
import com.samkruglov.base.api.view.mapper.config.CreatorMapperConfig;
import com.samkruglov.base.domain.Role;
import com.samkruglov.base.domain.User;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Mapper(config = CreatorMapperConfig.class)
public abstract class UserMapper {

    @Autowired
    protected PasswordEncoder passwordEncoder;

    public abstract GetUserDto toGetUserDto(User user);

    public User toUser(CreateUserDto userDto, List<Role> roles) {
        return new User(
                userDto.getFirstName(),
                userDto.getLastName(),
                userDto.getEmail(),
                passwordEncoder.encode(userDto.getPassword()),
                roles
        );
    }
}
