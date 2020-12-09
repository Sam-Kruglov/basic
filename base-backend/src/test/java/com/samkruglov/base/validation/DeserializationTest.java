package com.samkruglov.base.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.samkruglov.base.config.ValidationTest;
import lombok.SneakyThrows;
import lombok.Value;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Stream;

@Import(DeserializationTest.StubController.class)
public class DeserializationTest extends ValidationTest {

    @RestController
    @RequestMapping("/stub")
    static class StubController {

        @PostMapping
        void doStuff(@RequestParam Integer integerParam, @RequestBody StubDto dto) {
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

    @SneakyThrows
    @BeforeEach
    void setUp() {
        integerParam = 1;
        defaultDto = (ObjectNode) mapper.readTree(mapper.writeValueAsString(new StubDto()));
    }

    void sendData(String... expectedInvalidFields) {
        sendAndAssertFields(
                client -> client.post()
                                .uri(builder -> {
                                    builder.path("/stub");
                                    if (integerParam != null) builder.queryParam("integerParam", integerParam);
                                    return builder.build();
                                })
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(defaultDto)
                                .exchange(),
                expectedInvalidFields
        );
    }

    @ParameterizedTest
    @MethodSource("com.samkruglov.base.validation.DeserializationTest#bodyFieldNames")
    void invalid_body_field_type(String fieldName) {
        defaultDto.set(fieldName, TextNode.valueOf("someString"));
        sendData(fieldName);
    }

    @Test
    void invalid_nested_body_field_type() {
        ((ObjectNode) defaultDto.get("nested")).set("integer", TextNode.valueOf("someString"));
        sendData("nested.integer");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "someString")
    void invalid_query_param_type(String param) {
        integerParam = param;
        sendData("integerParam");
    }

    static Stream<String> bodyFieldNames() {
        return FieldUtils.getAllFieldsList(StubDto.class).stream().map(Field::getName);
    }
}
