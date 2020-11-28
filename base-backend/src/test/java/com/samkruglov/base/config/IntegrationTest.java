package com.samkruglov.base.config;

import com.samkruglov.base.client.EnhancedApiClient;
import com.samkruglov.base.domain.User;
import feign.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaDelete;
import java.util.stream.Stream;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class IntegrationTest {

    protected EnhancedApiClient apiClient;

    private EntityManager entityManager;

    @BeforeAll
    void setUp(@LocalServerPort int port, @Autowired EntityManagerFactory entityManagerFactory) {
        apiClient = new EnhancedApiClient("http://localhost:" + port, Logger.Level.FULL);
        //we could autowire entity manager but that one cannot start transactions
        entityManager = entityManagerFactory.createEntityManager();
        clearDatabase();
    }

    protected void clearDatabase() {
        //keep roles, they are kind of part of the schema
        deleteEntities(User.class);
        //without users in the database we can't be logged in
        logout();
    }

    protected void logout() {
        apiClient.setBearerToken(null);
    }

    private void deleteEntities(Class<?>... entities) {
        Stream.of(entities).forEach(this::deleteEntity);
    }

    /**
     * Will delete all rows from the database table of this entity, other dependant tables,
     * and will also evict all related Hibernate 2nd level cache entries.
     */
    private <T> void deleteEntity(Class<T> entityClass) {
        final CriteriaDelete<T> criteriaDelete = entityManager.getCriteriaBuilder().createCriteriaDelete(entityClass);
        //todo check up on https://github.com/hibernate/hibernate-orm/pull/3670
        criteriaDelete.from(entityClass);
        entityManager.getTransaction().begin();
        entityManager.createQuery(criteriaDelete).executeUpdate();
        entityManager.getTransaction().commit();
    }
}
