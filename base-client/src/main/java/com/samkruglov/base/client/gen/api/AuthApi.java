package com.samkruglov.base.client.gen.api;

import com.samkruglov.base.client.gen.ApiClient;
import com.samkruglov.base.client.gen.view.ChangePasswordDto;
import com.samkruglov.base.client.gen.view.ChangeUserPasswordDto;
import com.samkruglov.base.client.gen.view.CredentialsDto;
import com.samkruglov.base.client.gen.view.JwtDto;
import feign.*;

public interface AuthApi extends ApiClient.Api {

  /** @param changePasswordDto (required) */
  @RequestLine("PUT /api/auth/users/self/change-password")
  @Headers({
    "Content-Type: application/json",
    "Accept: application/json",
  })
  void changeMyPassword(ChangePasswordDto changePasswordDto);

  /**
   * @param email (required)
   * @param changeUserPasswordDto (required)
   */
  @RequestLine("PUT /api/auth/users/{email}/change-password")
  @Headers({
    "Content-Type: application/json",
    "Accept: application/json",
  })
  void changeUserPassword(
      @Param("email") String email, ChangeUserPasswordDto changeUserPasswordDto);

  /**
   * @param credentialsDto (required)
   * @return JwtDto
   */
  @RequestLine("POST /api/auth/login")
  @Headers({
    "Content-Type: application/json",
    "Accept: application/json",
  })
  JwtDto login(CredentialsDto credentialsDto);
}
