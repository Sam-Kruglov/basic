package com.samkruglov.base.repo.config;

import com.samkruglov.base.config.SecurityConfig;
import com.samkruglov.base.domain.User;
import com.samkruglov.base.repo.impl.JpaUserRepo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Optional;

@Configuration
@EnableJpaRepositories(
        basePackageClasses = JpaUserRepo.class,
        repositoryBaseClass = NaturalRepositoryImpl.class
)
@EnableTransactionManagement
@EnableJpaAuditing
public class JpaConfig {

    @Bean
    public AuditorAware<User> auditorAware() {
        return () -> Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                             .filter(Authentication::isAuthenticated)
                             .map(Authentication::getPrincipal)
                             .map(SecurityConfig.CustomUser.class::cast)
                             .map(SecurityConfig.CustomUser::getDelegate);
    }
}
