package com.toptal.screening.soccerplayermarket.config;

import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.toptal.screening.soccerplayermarket.domain.Role;
import com.toptal.screening.soccerplayermarket.domain.User;
import com.toptal.screening.soccerplayermarket.repo.RoleRepo;
import com.toptal.screening.soccerplayermarket.repo.UserRepo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.security.interfaces.RSAPrivateKey;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    public static final String ROLE_USER = "USER";
    public static final String ROLE_ADMIN = "ADMIN";

    //todo there isn't any Spring native JWT generation yet
    // but watch out for https://github.com/spring-projects-experimental/spring-authorization-server
    @Bean
    public JWSSigner jwsSigner(@Value("${jwt.private-key}") RSAPrivateKey privateKey) {
        return new RSASSASigner(privateKey);
    }

    @Configuration
    @RequiredArgsConstructor
    public static class ApiConfig extends WebSecurityConfigurerAdapter {
        private final UserDetailsService userDetailsService;

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.setSharedObject(SessionCreationPolicy.class, SessionCreationPolicy.STATELESS);
            /* see default behaviour in JwtGrantedAuthoritiesConverter:
             * It expects a role without any prefix inside the incoming JWT.
             * OAuth2 terminology uses "SCOPE_" prefix by default, so inside GrantedAuthority it will have it
             * instead of "ROLE_" that we're using here. But it is configurable.
             * I could just configure that prefix and it would work but then if a user is deleted,
             * token revocation needs to be implemented.
             * This way I will query the database every time, so if the user no longer exists but the token
             * is still valid the request will be properly rejected.
             * I added cache to user repository but when the project will scale, JWT revocation should be in place
             * so that we can use the JWT itself for the Authentication principal and not go the database for the User.
             */
            Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter = jwt -> {
                val userDetails = userDetailsService.loadUserByUsername(jwt.getSubject());
                return new UserDetailsJwtAuthenticationToken(userDetails, jwt);
            };
            //@formatter:off
            http
                    // JWT based authentication does not use cookies hence CSRF is impossible
                    .csrf().disable()
                    .authorizeRequests()
                        .antMatchers("/api/login")
                            .permitAll()
                        // there's no need for the already logged in users to create more users
                        // fixme attacker may infinitely create users. Rate limiting required
                        .antMatchers(HttpMethod.POST, "/api/users")
                            .access("not authenticated or hasRole('" + ROLE_ADMIN + "')")
                        //admin users can only be removed manually directly from the database
                        .antMatchers(HttpMethod.DELETE, "/api/users/self").not().hasRole(ROLE_ADMIN)
                        .antMatchers(HttpMethod.DELETE, "/api/users/{email}")
                            .access("hasRole('" + ROLE_ADMIN + "') and not @userRepo.findByEmail(#email).orElse(false).hasRole(" + ROLE_ADMIN + ")")
                        .antMatchers("/api/**/self/**").hasRole(ROLE_USER)
                        .antMatchers("/**").hasRole(ROLE_ADMIN)
                        .and()
                    .oauth2ResourceServer()
                        .jwt()
                        .jwtAuthenticationConverter(jwtAuthenticationConverter);
            //@formatter:on
        }

        @Override
        protected void configure(AuthenticationManagerBuilder auth) {
            //for username/password authentication inside /api/login
            val daoAuthenticationProvider = new DaoAuthenticationProvider();
            daoAuthenticationProvider.setUserDetailsService(userDetailsService);
            auth.authenticationProvider(daoAuthenticationProvider);
        }

        @Override
        @Bean
        public AuthenticationManager authenticationManagerBean() throws Exception {
            return super.authenticationManagerBean();
        }
    }

    /**
     * Use {@link BCryptPasswordEncoder#BCryptPasswordEncoder(int)}
     * to set the complexity of encoding so that it takes at least a second to verify a password.
     * <p>
     * Recommendation taken from Spring docs.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepo repo) {
        return username -> repo.findByEmail(username)
                               .map(CustomUser::new)
                               .orElseThrow(() -> new UsernameNotFoundException(username));
    }

    @Configuration
    @Profile("dev")
    public static class DevConfig {

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
        public void createDefaultAdmin(UserRepo userRepo, PasswordEncoder passwordEncoder, RoleRepo roleRepo) {
            val username = "admin@company.com";
            if (!userRepo.existsByEmail(username)) {
                userRepo.save(new User(
                        username,
                        passwordEncoder.encode("adminpass"),
                        List.of(roleRepo.findByName(ROLE_USER), roleRepo.findByName(ROLE_ADMIN))
                ));
            }
        }

        /**
         * Since it's the same JVM that creates and validates the tokens, there's no need
         * to have any buffer time period.
         * It may not be the same in production, so this is only done for dev,
         * so we can see token expiration instantly.
         */
        @Autowired
        @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
        public void removeJwtValidatorClockSkew(JwtDecoder jwtDecoder) {
            List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
            validators.add(new JwtTimestampValidator(Duration.ZERO));
            ((NimbusJwtDecoder) jwtDecoder).setJwtValidator(new DelegatingOAuth2TokenValidator<>(validators));
        }
    }

    @Getter
    public static class CustomUser extends org.springframework.security.core.userdetails.User {

        private final User delegate;

        public CustomUser(User delegate) {
            super(delegate.getEmail(), delegate.getEncodedPassword(), getAuthorities(delegate));
            this.delegate = delegate;
        }

        private static Collection<SimpleGrantedAuthority> getAuthorities(User user) {
            return user.getRoles()
                       .stream()
                       .map(Role::getName)
                       // in Spring Security terminology, role is "ADMIN" and authority is "ROLE_ADMIN"
                       .map(role -> "ROLE_" + role)
                       .map(SimpleGrantedAuthority::new)
                       .collect(toList());
        }
    }

    /**
     * see {@link JwtAuthenticationToken}
     */
    public static class UserDetailsJwtAuthenticationToken extends AbstractOAuth2TokenAuthenticationToken<Jwt> {

        public UserDetailsJwtAuthenticationToken(UserDetails userDetails, Jwt jwt) {
            super(jwt, userDetails, jwt, userDetails.getAuthorities());
            setAuthenticated(true);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Map<String, Object> getTokenAttributes() {
            return getToken().getClaims();
        }

        /**
         * The principal name which is, by default, the {@link Jwt}'s subject
         */
        @Override
        public String getName() {
            return getToken().getSubject();
        }
    }
}
