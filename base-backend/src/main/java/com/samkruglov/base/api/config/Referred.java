package com.samkruglov.base.api.config;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;

/**
 * see {@link ReferredUserConfig}
 */
@Parameter(
        name = "email",
        in = PATH,
        required = true,
        schema = @Schema(implementation = String.class, minLength = 2, maxLength = 70)
)
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Referred {
}
