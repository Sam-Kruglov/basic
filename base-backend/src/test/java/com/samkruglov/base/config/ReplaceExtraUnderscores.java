package com.samkruglov.base.config;

import org.junit.jupiter.api.DisplayNameGenerator;

import java.lang.reflect.Method;

/**
 * "given_x_and_y__return_z" -> "given x and y -- return z"
 */
public class ReplaceExtraUnderscores extends DisplayNameGenerator.ReplaceUnderscores {
    @Override
    public String generateDisplayNameForMethod(Class<?> testClass, Method testMethod) {
        return super.generateDisplayNameForMethod(testClass, testMethod).replace("  ", " -- ");
    }
}
