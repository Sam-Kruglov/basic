package com.samkruglov.base.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.samkruglov.base.api.config.Referred;
import com.samkruglov.base.api.config.error.ErrorResponse;
import com.samkruglov.base.config.ValidationTest;
import com.samkruglov.base.domain.User;
import com.samkruglov.base.service.error.BaseErrorType;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.val;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.samkruglov.base.api.config.UserUrlPathId.BY_EMAIL;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.willReturn;

@Import(DeserializationTest.StubController.class)
public class DeserializationTest extends ValidationTest {

    @RestController
    @RequestMapping("/stub")
    static class StubController {

        @PostMapping(BY_EMAIL)
        void doStuff(@Referred User user, @RequestParam Integer integerParam, @RequestBody StubDto dto) {
        }
    }

    @Value
    private static class StubDto {
        Integer       integer   = 1;
        List<Integer> list      = List.of(1);
        StubEnum      enumValue = StubEnum.ONE;
        Stub2Dto      nested    = new Stub2Dto();
    }

    private enum StubEnum {ONE}

    @Value
    private static class Stub2Dto {
        Integer integer = 1;
    }

    ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
    ObjectNode   defaultDto;
    Object       integerParam;
    Object       email;


    @SneakyThrows
    @BeforeEach
    void setUp() {
        integerParam = 1;
        defaultDto = (ObjectNode) mapper.readTree(mapper.writeValueAsString(new StubDto()));
        email = "john.smith@company.com";
    }

    private Function<WebTestClient, WebTestClient.ResponseSpec> buildSender() {
        return client -> client.post()
                               .uri(builder -> {
                                   builder.path("/stub/" + BY_EMAIL);
                                   if (integerParam != null) builder.queryParam("integerParam", integerParam);
                                   return builder.build(email);
                               })
                               .contentType(MediaType.APPLICATION_JSON)
                               .bodyValue(defaultDto)
                               .exchange();
    }

    void sendAndAssertFields(String... expectedInvalidFields) {
        sendAndAssertFields(
                buildSender(),
                expectedInvalidFields
        );
    }

    @Nested
    class body {

        @ParameterizedTest
        @MethodSource("com.samkruglov.base.validation.DeserializationTest#bodyFieldNames")
        void invalid_body_field_type(String fieldName) {
            defaultDto.set(fieldName, TextNode.valueOf("someString"));
            sendAndAssertFields(fieldName);
        }

        @Test
        void invalid_nested_body_field_type() {
            ((ObjectNode) defaultDto.get("nested")).set("integer", TextNode.valueOf("someString"));
            sendAndAssertFields("nested.integer");
        }
    }

    @Nested
    @ExtendWith(SoftAssertionsExtension.class)
    class referred_user {

        @Test
        void user_not_found(SoftAssertions softly) {
            //must do the stubbing backwards or it will treat it as an actual call
            willReturn(Optional.empty()).given(userRepo).findByEmail(anyString());

            val errorType = BaseErrorType.USER_NOT_FOUND;

            buildSender().apply(webTestClient)
                         .expectStatus().isEqualTo(errorType.httpStatusCode)
                         .expectBody(ErrorResponse.class)
                         .value(response -> {
                             softly.assertThat(response.getCode()).isEqualTo(errorType.errorCode);
                             softly.assertThat(response.getMessage()).contains("not found");
                         });
        }

        @Test
        void invalid_email() {
            email = "invalid";
            sendAndAssertFields("email");
        }
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "someString")
    void invalid_query_param_type(String param) {
        integerParam = param;
        sendAndAssertFields("integerParam");
    }

    static Stream<String> bodyFieldNames() {
        return FieldUtils.getAllFieldsList(StubDto.class).stream().map(Field::getName);
    }
}
