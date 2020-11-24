package com.samkruglov.base.config;

import com.samkruglov.base.client.error.BaseException;
import com.samkruglov.base.client.gen.view.ErrorResponse;
import feign.FeignException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.assertj.core.api.ThrowableAssertAlternative;
import org.assertj.core.api.ThrowableTypeAssert;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestUtil {

    public static ThrowableAssertAlternative<FeignException.Forbidden> assertNoPermissionTo(
            ThrowingCallable throwingCallable
    ) {
        return assertThatExceptionOfType(FeignException.Forbidden.class).isThrownBy(throwingCallable);
    }

    public static ThrowableTypeAssert<FeignException.Unauthorized> assertThatUnauthorized() {
        return assertThatExceptionOfType(FeignException.Unauthorized.class);
    }

    public static ThrowableTypeAssert<BaseException> assertThatBaseException(ErrorResponse.CodeEnum type) {
        return new BaseExceptionThrowableTypeAssert(type);
    }

    public static class BaseExceptionThrowableTypeAssert
            extends ThrowableTypeAssert<BaseException> {

        private final ErrorResponse.CodeEnum type;

        public BaseExceptionThrowableTypeAssert(ErrorResponse.CodeEnum type) {
            super(BaseException.class);
            this.type = type;
        }

        @Override
        public ThrowableAssertAlternative<BaseException> isThrownBy(ThrowingCallable throwingCallable) {
            return super.isThrownBy(throwingCallable).matches(e -> e.getErrorCode() == type);
        }
    }
}
