package com.samkruglov.base.config;

import com.samkruglov.base.client.gen.view.ErrorResponse;
import com.samkruglov.base.service.error.BaseErrorType;
import com.samkruglov.base.service.error.BaseException;
import feign.FeignException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.assertj.core.api.ThrowableAssertAlternative;
import org.assertj.core.api.ThrowableTypeAssert;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestUtil {

    public static ThrowableTypeAssert<BaseException> assertThatBaseException(BaseErrorType type) {
        return new BaseExceptionThrowableTypeAssert(type);
    }

    public static class BaseExceptionThrowableTypeAssert extends ThrowableTypeAssert<BaseException> {

        private final BaseErrorType type;

        public BaseExceptionThrowableTypeAssert(BaseErrorType type) {
            super(BaseException.class);
            this.type = type;
        }

        @Override
        public ThrowableAssertAlternative<BaseException> isThrownBy(ThrowingCallable throwingCallable) {
            return super.isThrownBy(throwingCallable).matches(e -> e.getErrorType() == type);
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Client {

        public static ThrowableAssertAlternative<FeignException.Forbidden> assertNoPermissionTo(
                ThrowingCallable throwingCallable
        ) {
            return assertThatExceptionOfType(FeignException.Forbidden.class).isThrownBy(throwingCallable);
        }

        public static ThrowableTypeAssert<FeignException.Unauthorized> assertThatUnauthorized() {
            return assertThatExceptionOfType(FeignException.Unauthorized.class);
        }

        public static ThrowableTypeAssert<com.samkruglov.base.client.error.BaseException> assertThatBaseException(
                ErrorResponse.CodeEnum type
        ) {
            return new BaseExceptionThrowableTypeAssert(type);
        }

        public static class BaseExceptionThrowableTypeAssert
                extends ThrowableTypeAssert<com.samkruglov.base.client.error.BaseException> {

            private final ErrorResponse.CodeEnum type;

            public BaseExceptionThrowableTypeAssert(ErrorResponse.CodeEnum type) {
                super(com.samkruglov.base.client.error.BaseException.class);
                this.type = type;
            }

            @Override
            public ThrowableAssertAlternative<com.samkruglov.base.client.error.BaseException>
            isThrownBy(ThrowingCallable throwingCallable) {
                return super.isThrownBy(throwingCallable).matches(e -> e.getErrorCode() == type);
            }
        }
    }
}
