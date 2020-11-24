package com.samkruglov.base.repo.config;

import lombok.val;
import org.hibernate.Session;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

@Transactional(readOnly = true)
public class NaturalRepositoryImpl<T, ID extends Serializable>
        extends SimpleJpaRepository<T, ID> implements HibernateRepository<T, ID> {

    private final EntityManager entityManager;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public NaturalRepositoryImpl(JpaEntityInformation<T, ?> entityInformation,
                                 EntityManager entityManager) {
        super(entityInformation, entityManager);

        this.entityManager = entityManager;
    }

    @Override
    public <N extends Serializable> Optional<T> findByNaturalId(N naturalId) {
        return entityManager.unwrap(Session.class)
                            .bySimpleNaturalId(this.getDomainClass())
                            .loadOptional(naturalId);
    }

    @Override
    public Optional<T> findByNaturalId(Map<String, Object> naturalIds) {
        val loadAccess = entityManager.unwrap(Session.class).byNaturalId(this.getDomainClass());
        naturalIds.forEach(loadAccess::using);
        return loadAccess.loadOptional();
    }

}