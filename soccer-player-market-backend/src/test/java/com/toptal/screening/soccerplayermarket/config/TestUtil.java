package com.toptal.screening.soccerplayermarket.config;

import com.toptal.screening.soccerplayermarket.client.error.SoccerPlayerMarketException;
import com.toptal.screening.soccerplayermarket.client.gen.view.ErrorResponse.CodeEnum;
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

    public static ThrowableTypeAssert<SoccerPlayerMarketException> assertThatSoccerPlayerMarketException(
            CodeEnum type
    ) {
        return new SoccerPlayerMarketExceptionThrowableTypeAssert(type);
    }

    public static class SoccerPlayerMarketExceptionThrowableTypeAssert
            extends ThrowableTypeAssert<SoccerPlayerMarketException> {

        private final CodeEnum type;

        public SoccerPlayerMarketExceptionThrowableTypeAssert(CodeEnum type) {
            super(SoccerPlayerMarketException.class);
            this.type = type;
        }

        @Override
        public ThrowableAssertAlternative<SoccerPlayerMarketException> isThrownBy(ThrowingCallable throwingCallable) {
            return super.isThrownBy(throwingCallable).matches(e -> e.getErrorCode() == type);
        }


    }
}
