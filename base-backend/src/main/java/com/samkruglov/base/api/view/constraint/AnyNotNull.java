package com.samkruglov.base.api.view.constraint;

import com.samkruglov.base.api.view.constraint.impl.AnyNotNullValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = AnyNotNullValidator.class)
@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Documented
public @interface AnyNotNull {

    /**
     * Property names
     */
    String[] value();

    String message() default "at least one field must not be null: {value}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}