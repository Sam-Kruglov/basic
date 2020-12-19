package com.samkruglov.base.api.config;

import com.samkruglov.base.domain.User;
import com.samkruglov.base.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.val;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.ValueConstants;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
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
        resolvers.add(new UserViaEmailPathVariableMethodArgumentResolver(userRepo, new EmailValidator(validator)));
    }

    @RequiredArgsConstructor
    private static class UserViaEmailPathVariableMethodArgumentResolver implements HandlerMethodArgumentResolver {

        private final EmailPathVariableMethodArgumentResolverDelegate delegate =
                new EmailPathVariableMethodArgumentResolverDelegate();
        private final UserRepo                                        userRepo;
        private final EmailValidator                                  emailValidator;

        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.hasParameterAnnotation(Referred.class)
                    && User.class.equals(parameter.getParameterType());
        }

        @Override
        public Object resolveArgument(
                MethodParameter parameter,
                ModelAndViewContainer mavContainer,
                NativeWebRequest webRequest,
                WebDataBinderFactory binderFactory
        ) throws Exception {
            // binder should be null to ignore the fact that the parameter is of type User,
            // otherwise it will try to convert and fail.
            val email = (String) delegate.resolveArgument(parameter, mavContainer, webRequest, null);
            emailValidator.validate(email);
            val referredAnnotation = parameter.getParameterAnnotation(Referred.class);
            //noinspection ConstantConditions annotation can't be null, checked in #supportsParameter
            if (referredAnnotation.lazy()) {
                return userRepo.getReferenceByEmail(email);
            }
            return userRepo.getByEmail(email);
        }
    }

    private static class EmailPathVariableMethodArgumentResolverDelegate extends PathVariableMethodArgumentResolver {

        @Override
        protected NamedValueInfo createNamedValueInfo(MethodParameter parameter) {
            return new NamedValueInfo(EMAIL_PATH_VARIABLE_NAME, true, ValueConstants.DEFAULT_NONE);
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
