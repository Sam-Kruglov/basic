package com.samkruglov.base.config;

import com.samkruglov.base.client.EnhancedApiClient;
import feign.Logger;
import org.hibernate.Cache;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ActiveProfiles;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class IntegrationTest {

    protected EnhancedApiClient apiClient;

    private ResourceDatabasePopulator resourceDatabasePopulator;
    private DataSource dataSource;
    private EntityManagerFactory entityManagerFactory;

    @BeforeAll
    void setUp(
            @LocalServerPort int port,
            @Autowired DataSource dataSource,
            @Autowired EntityManagerFactory entityManagerFactory
    ) {
        apiClient = new EnhancedApiClient("http://localhost:" + port, Logger.Level.FULL);
        resourceDatabasePopulator = new ResourceDatabasePopulator(new ClassPathResource("clear_db.sql"));
        this.dataSource = dataSource;
        this.entityManagerFactory = entityManagerFactory;
        clearDatabase();
    }

    protected void clearDatabase() {
        resourceDatabasePopulator.execute(dataSource);
        //evict database cache
        entityManagerFactory.getCache().unwrap(Cache.class).evictAllRegions();
        //without users in the database we can't be logged in
        logout();
    }

    protected void logout() {
        apiClient.setBearerToken(null);
    }
}
