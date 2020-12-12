package com.samkruglov.base.api.view.constraint.impl;

import com.samkruglov.base.api.view.constraint.AnyNotNull;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.beanutils.BeanUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;
import java.util.stream.Stream;

public class AnyNotNullValidator implements ConstraintValidator<AnyNotNull, Object> {

    private String[] fieldNames;

    @Override
    public void initialize(AnyNotNull constraintAnnotation) {
        fieldNames = constraintAnnotation.value();
    }

    @SneakyThrows
    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        if (fieldNames.length != 0) {
            return Stream.of(fieldNames).map(field -> readField(obj, field)).anyMatch(Objects::nonNull);
        }
        // checking all fields
        val fields = BeanUtils.describe(obj);
        val valid = fields.values().stream().anyMatch(Objects::nonNull);
        if (!valid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    context.getDefaultConstraintMessageTemplate()
                           .replace("{value}", fields.keySet().toString())
            ).addConstraintViolation();
        }
        return valid;
    }

    @SneakyThrows
    private Object readField(Object obj, String fieldName) {
        return BeanUtils.getProperty(obj, fieldName);
    }
}