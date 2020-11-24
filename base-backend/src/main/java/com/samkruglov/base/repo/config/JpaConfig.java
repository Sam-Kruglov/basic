package com.samkruglov.base.repo.config;

import com.samkruglov.base.repo.impl.JpaUserRepo;
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
