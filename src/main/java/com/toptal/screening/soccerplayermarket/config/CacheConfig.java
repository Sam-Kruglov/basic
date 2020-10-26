package com.toptal.screening.soccerplayermarket.config;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.hibernate.CacheEnvironment;
import org.springframework.boot.autoconfigure.hazelcast.HazelcastInstanceFactory;
import org.springframework.boot.autoconfigure.hazelcast.HazelcastProperties;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Configuration
public class CacheConfig {

    /**
     * Shared between Hibernate and Spring. Although Spring doesn't actually need it unless we {@link EnableCaching}.
     * Another option would be to exclude HazelcastAutoConfiguration
     * <p>
     * see {@link org.springframework.boot.autoconfigure.hazelcast.HazelcastAutoConfiguration}
     * see {@link com.hazelcast.hibernate.instance.HazelcastInstanceLoader#loadInstance()}
     */
    @Bean
    HazelcastInstance hazelcastInstance(HazelcastProperties properties, JpaProperties jpaProperties) throws IOException {
        Resource config = properties.resolveConfigLocation();
        HazelcastInstance instance;
        if (config != null) {
            instance = new HazelcastInstanceFactory(config).getHazelcastInstance();
        } else {
            instance = Hazelcast.newHazelcastInstance();
        }
        jpaProperties.getProperties().put(CacheEnvironment.HAZELCAST_INSTANCE_NAME, instance.getName());
        return instance;
    }
}
