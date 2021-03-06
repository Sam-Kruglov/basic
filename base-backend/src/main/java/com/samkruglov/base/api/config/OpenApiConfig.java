package com.samkruglov.base.api.config;

import com.samkruglov.base.api.UserController;
import com.samkruglov.base.api.config.error.ErrorResponse;
import com.samkruglov.base.service.error.BaseErrorType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.core.util.RefUtils;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP;
import static java.lang.String.format;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;


@Profile("!prod & !test")
@Configuration
public class OpenApiConfig {
    public static final String INSECURE_TAG = "insecure";

    private static final String JWT_SECURITY_SCHEME = "jwt";

    static {
        ModelResolver.enumsAsRef = true;
    }

    private final List<CommonResponse> commonResponses = new LinkedList<>();

    @Bean
    @DependsOn("modelConverterRegistrar")
    public OpenAPI openApi(
            @Value("${spring.application.name}") String applicationName,
            BuildProperties buildProperties
    ) {
        val components = new Components();
        components.addSecuritySchemes(JWT_SECURITY_SCHEME,
                new SecurityScheme()
                        .type(HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Authenticate via `/api/auth/login`")
        );
        addCommonSecurityResponses(components);
        addDefaultErrorResponse(components);
        val capitalizedApplicationName = Stream.of(applicationName.split("\\W"))
                                               .map(StringUtils::capitalize)
                                               .collect(Collectors.joining(" "));
        val description = buildProperties.get("description")
                                         //maven description adds extra spaces
                                         .replaceAll(" +", " ");
        return new OpenAPI()
                .info(new Info()
                        .title(capitalizedApplicationName + " API")
                        .description(description)
                        .version(buildProperties.getVersion())
                )
                .components(components)
                .addSecurityItem(new SecurityRequirement().addList(JWT_SECURITY_SCHEME));
    }

    private void addDefaultErrorResponse(Components components) {
        val schema = createSchema(components, ErrorResponse.class);
        val descriptionBuilder = new StringBuilder();
        Stream.of(BaseErrorType.values())
              .collect(groupingBy(e -> e.httpStatusCode))
              .forEach((httpStatusCode, errorTypes) -> {
                  descriptionBuilder.append(format("* %d:\n\n", httpStatusCode));
                  errorTypes.forEach(errorType -> descriptionBuilder.append(
                          //markdown only registers new line if there are 2 of them
                          format("  * %d - %s\n\n", errorType.errorCode, errorType.description)
                  ));
              });
        schema.description(descriptionBuilder.toString());
        val errorCode = (IntegerSchema) schema.getProperties().get("code");
        errorCode.setEnum(mapBaseErrorType(e -> e.errorCode));
        errorCode.addExtension("x-enum-varnames", mapBaseErrorType(Enum::name));
        errorCode.addExtension("x-enum-descriptions", mapBaseErrorType(e -> e.description));
        val key = ApiResponses.DEFAULT;
        val response = new ApiResponse()
                .description("other error responses")
                .content(new Content().addMediaType(
                        org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                        new MediaType().schema(new Schema<>().$ref(RefUtils.constructRef(schema.getName())))
                ));
        components.addResponses(key, response);
        val ref = response.$ref(key).get$ref();
        response.$ref(null);
        commonResponses.add(new CommonResponse(key, ref));
    }

    private <T> List<T> mapBaseErrorType(Function<BaseErrorType, T> mapF) {
        return Stream.of(BaseErrorType.values()).map(mapF).collect(toList());
    }

    private void addCommonSecurityResponses(Components components) {
        Stream.of(HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN).forEach(securityError -> {
                    val httpCodeStr = String.valueOf(securityError.value());
                    val response = new ApiResponse()
                            .description(securityError.getReasonPhrase())
                            .addHeaderObject(
                                    HttpHeaders.WWW_AUTHENTICATE,
                                    new Header()
                                            .description("[RFC 6750#3](https://tools.ietf.org/html/rfc6750#section-3)")
                                            .schema(new StringSchema())
                            )
                            .content(new Content());
                    components.addResponses(httpCodeStr, response);
                    val ref = response.$ref(httpCodeStr).get$ref();
                    response.$ref(null);
                    commonResponses.add(new CommonResponse(httpCodeStr, ref));
                }
        );
    }

    private Schema<?> createSchema(Components components, Class<?> clazz) {
        //gotta wait for the org.springdoc.core.converters.ModelConverterRegistrar bean to have all converters in place
        val resolvedSchema = ModelConverters.getInstance().readAllAsResolvedSchema(clazz);
        resolvedSchema.referencedSchemas.forEach(components::addSchemas);
        return resolvedSchema.schema;
    }

    /**
     * see https://swagger.io/docs/specification/describing-responses/#reuse
     */
    @Bean
    OperationCustomizer defaultResponsesOperationCustomizer() {
        return ((operation, handlerMethod) -> {
            commonResponses.forEach(commonResponse -> {
                val response = new ApiResponse().$ref(commonResponse.getReference());
                operation.getResponses().addApiResponse(commonResponse.getHttpStatusCode(), response);
            });
            return operation;
        });
    }

    /**
     * The only way to remove the global security requirement set it the {@link OpenAPI}
     * is to programmatically set it to an empty list on the operation.
     * If it's {@code null} then the global value will be used.
     * Since there is no any other purpose on that tag, we also remove it right away.
     */
    @Bean
    public OperationCustomizer insecureOperationCustomizer() {
        return (operation, handlerMethod) -> {
            if (operation.getTags() != null && operation.getTags().contains(INSECURE_TAG)) {
                operation.setSecurity(List.of());
                operation.getTags().remove(INSECURE_TAG);
            }
            return operation;
        };
    }

    @Bean
    public OperationCustomizer createUserOperationCustomizer() {
        return (operation, handlerMethod) -> {
            if (UserController.CREATE_USER_OP_ID.equals(operation.getOperationId())) {
                operation.addSecurityItem(new SecurityRequirement().addList(JWT_SECURITY_SCHEME));
                operation.addSecurityItem(new SecurityRequirement());
            }
            return operation;
        };
    }

    @lombok.Value
    private static class CommonResponse {
        String httpStatusCode;
        String reference;
    }
}
