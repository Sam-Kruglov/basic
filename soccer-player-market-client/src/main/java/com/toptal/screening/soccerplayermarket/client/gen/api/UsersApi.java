package com.toptal.screening.soccerplayermarket.client.gen.api;

import com.toptal.screening.soccerplayermarket.client.gen.ApiClient;
import com.toptal.screening.soccerplayermarket.client.gen.view.CreateUserDto;
import com.toptal.screening.soccerplayermarket.client.gen.view.GetUserDto;
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
}
