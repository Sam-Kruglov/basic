package com.samkruglov.base.config;

import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.samkruglov.base.api.config.ReferredUserConfig;
import com.samkruglov.base.api.config.error.ErrorResponse;
import com.samkruglov.base.api.config.error.InvalidRequestParameter;
import com.samkruglov.base.repo.UserRepo;
import com.samkruglov.base.service.error.BaseErrorType;
import lombok.val;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.security.authentication.TestingAuthenticationProvider;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

/**
 * @implSpec use {@code @Import(SomeController.class)} for each test
 * @implNote if {@link WebMvcTest#controllers} is empty, it will load all controllers but the context will fail
 * due to missing beans that are injected into each controller. The idea is to test one controller per subclass,
 * so we need a way to supply just one controller to that field. We must also keep the security exclusion filter
 * since it brings no value in the scope of a validation test. One way would be to create a meta-annotation
 * that accepts a controller class and passes it to the {@code @WebMvcTest} via {@link AliasFor} but the {@link Import}
 * seems to work just fine. And to not load any controllers by default, we must create an exclusion filter.
 */
@WebMvcTest(
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class),
                @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Controller.class)
        }
)
@Import({ ValidationTest.InsecureConfig.class, ReferredUserConfig.class })
@ActiveProfiles("test")
public class ValidationTest {

    protected static String build_long_string(int size) {
        return IntStream.range(0, size).mapToObj(i -> "a").collect(joining());
    }

    protected           WebTestClient webTestClient;
    @MockBean protected UserRepo      userRepo;

    //todo check up on https://github.com/spring-projects/spring-boot/issues/23067
    @BeforeAll
    void setUpForAll(@Autowired MockMvc mockMvc) {
        webTestClient = MockMvcWebTestClient.bindTo(mockMvc)
                                            .codecs(c -> c.customCodecs().register(new Jackson2JsonDecoder(
                                                    Jackson2ObjectMapperBuilder.json().modules(new ParameterNamesModule()).build()
                                            )))
                                            .build();
    }

    @BeforeEach
    void setUpForEach() {
        given(userRepo.findByEmail(anyString()))
                .willAnswer(invocation -> Optional.of(UserTestFactory.createUser(invocation.getArgument(0))));
        given(userRepo.getByEmail(anyString())).willCallRealMethod();
        val user = UserTestFactory.createUser("john.smith@company.com");
        Authentication auth = new TestingAuthenticationToken(new SecurityConfig.CustomUser(user), null);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    protected void sendAndAssertFields(
            Function<WebTestClient, WebTestClient.ResponseSpec> sender,
            String... expectedInvalidFields
    ) {
        sendAndAssert(sender, (softly, response) -> {
            softly.assertThat(response.getMessage()).isNotBlank();
            softly.assertThat(response.getInvalidRequestParameters())
                  .extracting(InvalidRequestParameter::getMessage)
                  .allSatisfy(message -> assertThat(message).isNotBlank());
            softly.assertThat(response.getInvalidRequestParameters())
                  .extracting(InvalidRequestParameter::getName)
                  .contains(expectedInvalidFields);
        });
    }

    protected void sendAndAssertMessage(
            Function<WebTestClient, WebTestClient.ResponseSpec> sender,
            String... expectedMessageParts
    ) {
        sendAndAssert(sender, (softly, response) -> {
            softly.assertThat(response.getMessage()).contains(expectedMessageParts);
            softly.assertThat(response.getInvalidRequestParameters()).isNullOrEmpty();
        });
    }

    protected void sendAndAssertValid(
            Function<WebTestClient, WebTestClient.ResponseSpec> sender
    ) {
        sender.apply(webTestClient).expectStatus().is2xxSuccessful();
    }

    private void sendAndAssert(
            Function<WebTestClient, WebTestClient.ResponseSpec> sender,
            BiConsumer<SoftAssertions, ErrorResponse> asserter
    ) {
        sender.apply(webTestClient)
              .expectStatus().isBadRequest()
              .expectBody(ErrorResponse.class)
              .value(response -> SoftAssertions.assertSoftly(softly -> {
                  softly.assertThat(response.getCode()).isEqualTo(BaseErrorType.INVALID_REQUEST.errorCode);
                  asserter.accept(softly, response);
              }));
    }

    @TestConfiguration
    static class InsecureConfig extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.csrf().disable().authorizeRequests().anyRequest().permitAll();
        }

        @Override
        protected void configure(AuthenticationManagerBuilder auth) {
            auth.authenticationProvider(new TestingAuthenticationProvider());
        }
    }
}
