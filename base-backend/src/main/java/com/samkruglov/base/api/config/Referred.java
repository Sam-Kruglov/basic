package com.samkruglov.base.api.config;

import com.samkruglov.base.domain.User;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import org.hibernate.SimpleNaturalIdLoadAccess;

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

    /**
     * If {@code true}, will resolve to a lazy proxy returned by
     * {@link SimpleNaturalIdLoadAccess#getReference},
     * otherwise will return a real {@link User} object.
     * <p>
     * It's the best practice to set to {@code true} if you only need the primary key of the user.
     * <p>
     * see https://vladmihalcea.com/manytoone-jpa-hibernate/
     */
    boolean lazy() default false;
}
