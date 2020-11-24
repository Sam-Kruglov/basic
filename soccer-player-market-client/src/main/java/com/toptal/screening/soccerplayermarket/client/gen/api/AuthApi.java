package com.toptal.screening.soccerplayermarket.client.gen.api;

import com.toptal.screening.soccerplayermarket.client.gen.ApiClient;
import com.toptal.screening.soccerplayermarket.client.gen.view.CredentialsDto;
import com.toptal.screening.soccerplayermarket.client.gen.view.JwtDto;
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
