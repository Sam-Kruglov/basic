package com.samkruglov.base.api.view.constraint.impl;

import com.samkruglov.base.api.view.constraint.AnyNotNull;
import lombok.SneakyThrows;
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

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        return Stream.of(fieldNames).map(field -> readField(obj, field)).anyMatch(Objects::nonNull);
    }

    @SneakyThrows
    private Object readField(Object obj, String fieldName) {
        return BeanUtils.getProperty(obj, fieldName);
    }
}