package com.samkruglov.base.api.view.mapper;

import com.samkruglov.base.api.view.GetUserDto;
import com.samkruglov.base.domain.User;
import org.mapstruct.Mapper;

@Mapper
public interface UserMapper {

    GetUserDto toGetUserDto(User user);
}
