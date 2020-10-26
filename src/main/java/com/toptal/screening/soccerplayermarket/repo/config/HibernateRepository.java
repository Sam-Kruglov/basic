package com.toptal.screening.soccerplayermarket.repo.config;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

/**
 * Hibernate can cache entities by its primary key as well as by its natual id.
 * Spring Data does not support this, so, we can extend it.
 * <p>
 * see https://jira.spring.io/browse/DATAJPA-1805
 */
@NoRepositoryBean
public interface HibernateRepository<T, ID extends Serializable> extends Repository<T, ID> {

    <N extends Serializable> Optional<T> findByNaturalId(N naturalId);

    Optional<T> findByNaturalId(Map<String, Object> naturalIds);
}
