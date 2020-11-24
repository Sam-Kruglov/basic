package com.samkruglov.base.client.gen.api;

import com.samkruglov.base.client.gen.ApiClient;
import com.samkruglov.base.client.gen.view.CredentialsDto;
import com.samkruglov.base.client.gen.view.JwtDto;
import feign.*;

public interface AuthApi extends ApiClient.Api {

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
