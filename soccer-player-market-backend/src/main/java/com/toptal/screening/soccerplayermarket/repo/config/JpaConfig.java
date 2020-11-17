package com.toptal.screening.soccerplayermarket.repo.config;

import com.toptal.screening.soccerplayermarket.repo.impl.JpaUserRepo;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(
        basePackageClasses = JpaUserRepo.class,
        repositoryBaseClass = NaturalRepositoryImpl.class
)
@EnableTransactionManagement
public class JpaConfig {
}
