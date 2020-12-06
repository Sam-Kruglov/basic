package com.samkruglov.base.config;

import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.samkruglov.base.domain.Role;
import com.samkruglov.base.domain.User;
import com.samkruglov.base.repo.RoleRepo;
import com.samkruglov.base.repo.UserRepo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
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
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.security.interfaces.RSAPrivateKey;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.samkruglov.base.config.Roles.ADMIN;
import static com.samkruglov.base.config.Roles.USER;
import static java.util.stream.Collectors.toList;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public JWSSigner jwsSigner(@Value("${jwt.private-key}") RSAPrivateKey privateKey) {
        return new RSASSASigner(privateKey);
    }

    @RequiredArgsConstructor
    public static class ApiConfig extends WebSecurityConfigurerAdapter {
        private final UserDetailsService userDetailsService;
        private final PasswordEncoder passwordEncoder;

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            /* see default behaviour in JwtGrantedAuthoritiesConverter:
             * It expects a role without any prefix inside the incoming JWT.
             * OAuth2 terminology uses "SCOPE_" prefix by default, so inside GrantedAuthority it will have it
             * instead of "ROLE_" that we're using here. But it is configurable.
             * We could just configure that prefix and it would work but we can't rely on JWT yet.
             * If a user is deleted, token revocation needs to be implemented.
             * Currently, we query the database every time, so if the user no longer exists but the token
             * is still valid the request is properly rejected.
             * There is a user cache but when the project will scale, JWT revocation should be in place so that we can
             * use the JWT itself for the Authentication principal and not go the database for the User every time.
             * Non-expired token ids could be stored in the distributed cache.
             */
            Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter = jwt -> {
                val userDetails = userDetailsService.loadUserByUsername(jwt.getSubject());
                return new UserDetailsJwtAuthenticationToken(userDetails, jwt);
            };
            //@formatter:off
            http
                    // JWT based authentication does not use cookies hence CSRF is impossible
                    .csrf().disable()
                    .sessionManagement()
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                        .and()
                    .authorizeRequests()
                        // /open-api doesn't exist in production
                        .antMatchers("/open-api/**").permitAll()
                        .antMatchers("/api/auth/login").permitAll()
                        .antMatchers("/api/auth/change-password").authenticated()
                        // there's no need for the already logged in users to create more users
                        // fixme attacker may infinitely create users. Rate limiting required
                        .antMatchers(HttpMethod.POST, "/api/users")
                            .access("not authenticated or hasRole(@roles.ADMIN)")
                        //admin users can only be removed manually directly from the database
                        .antMatchers(HttpMethod.DELETE, "/api/users/self").not().hasRole(ADMIN)
                        .antMatchers("/api/**/self/**").hasRole(USER)
                        .anyRequest().hasRole(ADMIN)
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
            daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
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
    @Profile("prod")
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public UserDetailsService userDetailsService(UserRepo repo) {
        return username -> repo.findByEmail(username)
                               .map(CustomUser::new)
                               .orElseThrow(() -> new UsernameNotFoundException(username));
    }

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
        @Bean
        public ApplicationListener<ApplicationReadyEvent> createDefaultAdmin(
                UserRepo userRepo,
                PasswordEncoder passwordEncoder,
                RoleRepo roleRepo
        ) {
            return event -> {
                val anyName = "admin";
                val email = anyName + "." + anyName + "@company.com";
                if (!userRepo.existsByEmail(email)) {
                    userRepo.save(new User(
                            anyName,
                            anyName,
                            email,
                            passwordEncoder.encode("adminpass"),
                            List.of(roleRepo.findByName(USER), roleRepo.findByName(ADMIN))
                    ));
                }
            };
        }
    }

    @Profile("dev | test")
    public static class LocalConfig {

        @Bean
        // it's not actually deprecated, just an indication to not use in production
        @SuppressWarnings("deprecation")
        public PasswordEncoder passwordEncoder() {
            return NoOpPasswordEncoder.getInstance();
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
            ((NimbusJwtDecoder) jwtDecoder).setJwtValidator(new JwtTimestampValidator(Duration.ZERO));
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
     * Adds {@link UserDetails} into the token.
     * <p>
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
