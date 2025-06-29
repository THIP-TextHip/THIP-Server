package konkuk.thip.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestQuerydslConfig {

    @PersistenceContext
    EntityManager em;

    @Bean
    JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(em);
    }
}
