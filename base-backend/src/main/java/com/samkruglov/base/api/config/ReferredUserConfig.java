package com.samkruglov.base.api.config;

import com.samkruglov.base.domain.User;
import com.samkruglov.base.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.val;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.bind.annotation.ValueConstants;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.PathVariableMethodArgumentResolver;

import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

import static com.samkruglov.base.api.config.UserUrlPathId.EMAIL_PATH_VARIABLE_NAME;

/**
 * Turns this:
 * <pre>{@code
 * @GetMapping("/users/{email}")
 * DTO getUser(@Email @PathVariable String email){
 *     val user = userRepo.getByEmail(email);
 *     ...
 * }
 * }</pre>
 * Into this:
 * <pre>{@code
 * @GetMapping("/users/{email}")
 * DTO getUser(@Referred User user){
 *     ...
 * }
 * }</pre>
 * <p>
 * see usages of {@link Referred}
 */
@Configuration
@RequiredArgsConstructor
public class ReferredUserConfig implements WebMvcConfigurer {

    private final UserRepo  userRepo;
    private final Validator validator;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new UserViaEmailPathVariableMethodArgumentResolver());
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new ValidEmailToUserConverter(userRepo, new EmailValidator(validator)));
    }

    private static class UserViaEmailPathVariableMethodArgumentResolver extends PathVariableMethodArgumentResolver {

        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.hasParameterAnnotation(Referred.class)
                    && User.class.equals(parameter.getParameterType());
        }

        @Override
        protected NamedValueInfo createNamedValueInfo(MethodParameter parameter) {
            return new NamedValueInfo(EMAIL_PATH_VARIABLE_NAME, true, ValueConstants.DEFAULT_NONE);
        }
    }

    /**
     * @implNote type conversion happens before validation
     * but we should validate the email before going to the database.
     */
    @RequiredArgsConstructor
    private static class ValidEmailToUserConverter implements Converter<String, User> {
        private final UserRepo       userRepo;
        private final EmailValidator emailValidator;

        @Override
        public User convert(String email) {
            emailValidator.validate(email);
            return userRepo.getByEmail(email);
        }
    }

    /**
     * @implNote {@link Validator} can either validate method parameters or an object.
     */
    @RequiredArgsConstructor
    private static class EmailValidator {

        private final Validator validator;

        void validate(String email) {
            val violations = validator.validateProperty(new EmailHolder(email), EMAIL_PATH_VARIABLE_NAME);
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(null, violations);
            }
        }

        @Value
        private static class EmailHolder {
            @NotBlank @Email @Size(min = 2, max = 70) String email;
        }
    }
}
