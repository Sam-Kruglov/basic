package com.toptal.screening.soccerplayermarket.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.toptal.screening.soccerplayermarket.client.error.SoccerPlayerMarketException;
import com.toptal.screening.soccerplayermarket.client.gen.ApiClient;
import com.toptal.screening.soccerplayermarket.client.gen.api.AuthApi;
import com.toptal.screening.soccerplayermarket.client.gen.view.CredentialsDto;
import com.toptal.screening.soccerplayermarket.client.gen.view.ErrorResponse;
import com.toptal.screening.soccerplayermarket.client.gen.view.JwtDto;
import feign.Feign;
import feign.Logger;
import feign.Response;
import feign.codec.ErrorDecoder;

import java.io.IOException;

/**
 * This class was created manually to improve the capabilities of the generated {@link ApiClient}
 */
public class EnhancedApiClient {

    private final ApiClient apiClient;
    private AuthApi authApi;

    public EnhancedApiClient(String basePath, Logger.Level logLevel) {
        apiClient = new ApiClient("jwt");
        apiClient.setBasePath(basePath);
        getFeignBuilder().logLevel(logLevel)
                         .decode404()
                         .errorDecoder(new SoccerPlayerMarketErrorDecoder(getObjectMapper()));
    }

    public void authenticate(String email, String password) {
        buildClient(AuthApi.class);
        JwtDto jwtDto = authApi.login(new CredentialsDto().email(email).password(password));
        setBearerToken(jwtDto.getJwt());
    }

    public void setBearerToken(String bearerToken) {
        apiClient.setBearerToken(bearerToken);
    }

    @SuppressWarnings("unchecked")
    public <T extends ApiClient.Api> T buildClient(Class<T> clientClass) {
        if (clientClass.equals(AuthApi.class)) {
            if (authApi == null) {
                authApi = apiClient.buildClient(AuthApi.class);
            }
            return (T) authApi;
        }
        return apiClient.buildClient(clientClass);
    }

    public Feign.Builder getFeignBuilder() {
        return apiClient.getFeignBuilder();
    }

    public void setFeignBuilder(Feign.Builder feignBuilder) {
        apiClient.setFeignBuilder(feignBuilder);
    }

    public ObjectMapper getObjectMapper() {
        return apiClient.getObjectMapper();
    }

    public String getBasePath() {
        return apiClient.getBasePath();
    }

    private static class SoccerPlayerMarketErrorDecoder implements ErrorDecoder {

        private final ErrorDecoder defaultDecoder = new ErrorDecoder.Default();
        private final ObjectMapper objectMapper;

        private SoccerPlayerMarketErrorDecoder(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @Override
        public Exception decode(String methodKey, Response response) {
            if (response.body() == null) {
                return defaultDecoder.decode(methodKey, response);
            }
            ErrorResponse errorResponse;
            try {
                errorResponse = objectMapper.readValue(response.body().asInputStream(), ErrorResponse.class);
            } catch (IOException e) {
                return defaultDecoder.decode(methodKey, response);
            }
            return new SoccerPlayerMarketException(errorResponse.getCode(), errorResponse.getMessage());
        }
    }
}
