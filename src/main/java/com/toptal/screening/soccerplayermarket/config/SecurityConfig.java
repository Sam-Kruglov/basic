package com.toptal.screening.soccerplayermarket.config;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    public static final String ROLE_USER = "USER";
    public static final String ROLE_ADMIN = "ADMIN";


    /**
     * Use {@link org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder#BCryptPasswordEncoder(int)}
     * to set the complexity of encoding so that it takes at least a second to verify a password.
     * <p>
     * Recommendation taken from Spring docs.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public UserDetailsManager userDetailsManager(DataSource dataSource) {
        return new JdbcUserDetailsManager(dataSource) {
            @Override
            public void deleteUser(String username) {
                val isAdmin = loadUserAuthorities(username).stream()
                        .map(GrantedAuthority::getAuthority)
                        .anyMatch(authority -> authority.equals("ROLE_" + ROLE_ADMIN));
                if (isAdmin) {
                    throw new AccessDeniedException("admin users can only be removed manually directly from the database");
                }
                super.deleteUser(username);
            }
        };
    }

    @Configuration
    @Profile("!dev")
    public static class ApiSecurity extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            //@formatter:off
            http
                    .authorizeRequests()
                        // there's no need for the already logged in users to create more users
                        // fixme attacker may infinitely create users. Rate limiting required
                        .antMatchers( HttpMethod.POST, "/api/users")
                            .access("not authenticated or hasRole('" + ROLE_ADMIN + "')")
                        .antMatchers("/api/**/me/**", "/api/**/my/**").hasRole(ROLE_USER)
                        .antMatchers("/**").hasRole(ROLE_ADMIN)
                        .and()
                    // fixme do not use basic auth in production
                    .httpBasic();
            //@formatter:on
        }


    }

    @Configuration
    @Profile("dev")
    public static class DevConfig {

        @Configuration
        public static class ApiSecurityDev extends ApiSecurity {

            @Override
            protected void configure(HttpSecurity http) throws Exception {
                super.configure(
                        // CSRF protection is only relevant when the API is used by a browser
                        http.csrf().disable()
                );
            }
        }

        /**
         * To create admins in prod, one must directly insert it into the database.
         * Preferably, LDAP integration or other should be configured.
         * See available integrations in the docs https://docs.spring.io/spring-security/site/docs/current/reference/html5
         * <p>
         * <p>
         * passwords can be encoded using the Spring CLI
         * https://docs.spring.io/spring-security/site/docs/current/reference/html5/#authentication-password-storage-boot-cli
         */
        @Autowired
        public void createDefaultAdmin(UserDetailsManager userDetailsManager, PasswordEncoder passwordEncoder) {
            val username = "admin";
            if (!userDetailsManager.userExists(username)) {
                userDetailsManager.createUser(User.builder().username(username)
                        .passwordEncoder(passwordEncoder::encode)
                        .password(username)
                        .roles(ROLE_USER, ROLE_ADMIN)
                        .build());
            }
        }
    }
}
