package com.samkruglov.base.repo.config;

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

    Optional<T> findByNaturalIds(Map<String, Object> naturalIds);

    /**
     * Only queries the database for the id and constructs a lazy proxy.
     */
    <N extends Serializable> Optional<T> findReferenceByNaturalId(N naturalId);

    /**
     * Only queries the database for the id and constructs a lazy proxy.
     */
    Optional<T> findReferenceByNaturalIds(Map<String, Object> naturalIds);

    /**
     * Does not query the database and constructs a lazy proxy.
     * If such entity isn't found, throws {@link javax.persistence.EntityNotFoundException}
     * see {@link javax.persistence.EntityManager#getReference}
     */
    T getReferenceById(ID id);
}
