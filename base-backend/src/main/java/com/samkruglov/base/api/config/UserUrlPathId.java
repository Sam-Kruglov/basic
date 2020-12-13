package com.samkruglov.base.api.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserUrlPathId {
    static final String EMAIL_PATH_VARIABLE_NAME = "email";

    /**
     * Resolve user by authentication.
     * see usages of {@link Current}
     */
    public static final String SELF = "self";

    /**
     * Resolve user by email path variable.
     * see usages of {@link Referred}
     */
    public static final String BY_EMAIL = "{" + EMAIL_PATH_VARIABLE_NAME + "}";
}
