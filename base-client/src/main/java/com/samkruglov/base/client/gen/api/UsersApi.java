package com.samkruglov.base.client.gen.api;

import com.samkruglov.base.client.gen.ApiClient;
import com.samkruglov.base.client.gen.view.CreateUserDto;
import com.samkruglov.base.client.gen.view.GetUserDto;
import com.samkruglov.base.client.gen.view.UpdateUserDto;
import feign.*;

public interface UsersApi extends ApiClient.Api {

  /** @param createUserDto (required) */
  @RequestLine("POST /api/users")
  @Headers({
    "Content-Type: application/json",
    "Accept: application/json",
  })
  void createUser(CreateUserDto createUserDto);

  /** @return GetUserDto */
  @RequestLine("GET /api/users/self")
  @Headers({
    "Accept: application/json",
  })
  GetUserDto getMe();

  /**
   * @param email (required)
   * @return GetUserDto
   */
  @RequestLine("GET /api/users/{email}")
  @Headers({
    "Accept: application/json",
  })
  GetUserDto getUser(@Param("email") String email);

  /** */
  @RequestLine("DELETE /api/users/self")
  @Headers({
    "Accept: application/json",
  })
  void removeMe();

  /** @param email (required) */
  @RequestLine("DELETE /api/users/{email}")
  @Headers({
    "Accept: application/json",
  })
  void removeUser(@Param("email") String email);

  /** @param updateUserDto (required) */
  @RequestLine("PUT /api/users/self")
  @Headers({
    "Content-Type: application/json",
    "Accept: application/json",
  })
  void updateMe(UpdateUserDto updateUserDto);

  /**
   * @param email (required)
   * @param updateUserDto (required)
   */
  @RequestLine("PUT /api/users/{email}")
  @Headers({
    "Content-Type: application/json",
    "Accept: application/json",
  })
  void updateUser(@Param("email") String email, UpdateUserDto updateUserDto);
}
