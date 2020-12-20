package com.samkruglov.base.api.view.mapper;

import com.samkruglov.base.api.view.mapper.config.CreatorMapperConfig;
import com.samkruglov.base.api.view.mapper.config.UpdaterMapperConfig;
import com.samkruglov.base.api.view.request.CreateUserDto;
import com.samkruglov.base.api.view.request.UpdateUserDto;
import com.samkruglov.base.api.view.response.GetUserDto;
import com.samkruglov.base.domain.Role;
import com.samkruglov.base.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

@Mapper(config = CreatorMapperConfig.class)
public abstract class UserMapper {

    @Autowired
    protected Updater updater;

    public abstract GetUserDto toGetUserDto(User user);

    public void updateUser(User user, UpdateUserDto updateDto) {
        updater.updateUser(user, updateDto);
    }

    //todo check up on https://github.com/mapstruct/mapstruct/issues/2285
    public User toUser(CreateUserDto userDto, String password, Set<Role> roles) {
        return new User(
                userDto.getFirstName(),
                userDto.getLastName(),
                userDto.getEmail(),
                password,
                roles
        );
    }

    @Mapper(config = UpdaterMapperConfig.class)
    interface Updater {

        void updateUser(@MappingTarget User user, UpdateUserDto updateDto);
    }
}
