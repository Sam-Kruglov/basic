package com.samkruglov.base.config;

import com.samkruglov.base.client.EnhancedApiClient;
import com.samkruglov.base.domain.User;
import feign.Logger;
import org.junit.jupiter.api.AfterAll;
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
    protected UserTestFactory userFactory;

    private EntityManager entityManager;

    @BeforeAll
    void setUp(
            @LocalServerPort int port,
            @Autowired UserTestFactory userFactory,
            @Autowired EntityManagerFactory entityManagerFactory
    ) {
        apiClient = new EnhancedApiClient("http://localhost:" + port, Logger.Level.FULL);
        this.userFactory = userFactory;
        //we could autowire entity manager but that one cannot start transactions
        entityManager = entityManagerFactory.createEntityManager();
    }

    @AfterAll
    void cleanUp() {
        entityManager.close();
    }

    protected void clearDatabase() {
        //keep roles, they are kind of part of the schema
        deleteEntities(User.class);
        //without users in the database we can't be logged in
        logout();
    }

    protected void login(String email) {
        apiClient.authenticate(email, UserTestFactory.PASSWORD);
    }

    protected void logout() {
        apiClient.setBearerToken(null);
    }

    private void deleteEntities(Class<?>... entities) {
        entityManager.getTransaction().begin();
        Stream.of(entities).forEach(this::deleteEntity);
        entityManager.getTransaction().commit();
    }

    /**
     * Will delete all rows from the database table of this entity, other dependant tables,
     * and will also evict all related Hibernate 2nd level cache entries.
     */
    private <T> void deleteEntity(Class<T> entityClass) {
        final CriteriaDelete<T> criteriaDelete = entityManager.getCriteriaBuilder().createCriteriaDelete(entityClass);
        //todo check up on https://github.com/hibernate/hibernate-orm/pull/3670
        criteriaDelete.from(entityClass);
        entityManager.createQuery(criteriaDelete).executeUpdate();
    }
}
