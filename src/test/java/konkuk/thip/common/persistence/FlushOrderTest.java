package konkuk.thip.common.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * flush 시점에 INSERT가 UPDATE보다 항상 먼저 실행되는지 검증하는 테스트.
 *
 * 로그에서 직접 SQL 순서를 눈으로 확인할 것.
 * application-test.yml 또는 test/resources에서
 * spring.jpa.show-sql=true 또는 logging.level.org.hibernate.SQL=debug 설정 필요.
 *
 * [테스트 3종]
 *   1. 코드 순서 UPDATE → INSERT : flush 시 INSERT 먼저 (핵심 케이스)
 *   2. 코드 순서 UPDATE → flush() → INSERT : 강제 flush로 분리된 케이스
 *   3. 코드 순서 INSERT → UPDATE : 자연스러운 순서 기준선
 */
@DataJpaTest
@Transactional  // @DataJpaTest가 이미 포함하지만, 트랜잭션 범위를 명시적으로 표기
@ActiveProfiles("test")
@EnableJpaRepositories(
        considerNestedRepositories = true,
        basePackageClasses = FlushOrderTest.class
)
@EntityScan(basePackageClasses = FlushOrderTest.class)
public class FlushOrderTest {

    @Autowired
    EntityManager em;

    @Entity
    @Getter
    @Setter
    @NoArgsConstructor
    static class TestUser {
        @Id
        @GeneratedValue
        private Long id;
        private String name;

        public TestUser(String name) {
            this.name = name;
        }
    }

    private Long existingId;

    @BeforeEach
    void setUp() {
        TestUser user = new TestUser("기존유저");
        em.persist(user);
        em.flush();   // INSERT → DB 반영 (각 테스트의 UPDATE 대상 사전 확보)
        em.clear();   // 영속성 컨텍스트 초기화 → 이후 em.find() 시 DB에서 다시 로드
        existingId = user.getId();
    }

    @Test
    @DisplayName("[코드 순서: UPDATE → INSERT] flush 시 INSERT가 항상 먼저 실행된다")
    void update_then_insert_in_code__insert_first_at_flush() {
        /**
         * 코드 작성 순서는 UPDATE → INSERT 이지만,
         * flush 시점에는 Hibernate ActionQueue가 INSERT를 먼저 처리한다.
         *
         * 예상 SQL 로그:
         *   insert into test_user (name) values ('신규유저')
         *   update test_user set name='수정된유저' where id=?
         */

        // UPDATE 대상 설정 (코드 상 먼저)
        TestUser existing = em.find(TestUser.class, existingId);
        existing.setName("수정된유저");     // dirty → flush 시 UPDATE

        // INSERT 대상 설정 (코드 상 나중)
        em.persist(new TestUser("신규유저"));

        em.flush();     // ← 여기서 SQL 로그 확인: INSERT(신규유저) 가 UPDATE(수정된유저) 보다 먼저 나와야 함
    }

    @Test
    @DisplayName("[코드 순서: UPDATE → flush() → INSERT] 강제 flush 이후 INSERT는 별도 flush에서 실행된다")
    void update_then_force_flush_then_insert() {
        /**
         * 강제 flush를 중간에 끼워 넣으면, 그 시점까지 누적된 변경사항만 반영된다.
         * INSERT 대상은 강제 flush 이후에 등록되었으므로 다음 flush에서 처리된다.
         *
         * 예상 SQL 로그:
         *   [첫 번째 flush]  update test_user set name='수정된유저' where id=?
         *   [두 번째 flush]  insert into test_user (name) values ('신규유저')
         */

        // UPDATE 대상 설정
        TestUser existing = em.find(TestUser.class, existingId);
        existing.setName("수정된유저");

        em.flush();     // ← 첫 번째 flush: UPDATE(수정된유저) 만 나감

        // INSERT 대상 설정 (강제 flush 이후)
        em.persist(new TestUser("신규유저"));

        em.flush();     // ← 두 번째 flush: INSERT(신규유저) 만 나감
    }

    @Test
    @DisplayName("[코드 순서: INSERT → UPDATE] 자연스러운 코드 순서에서도 INSERT가 먼저 실행된다 (기준선)")
    void insert_then_update_in_code__insert_first_at_flush() {
        /**
         * 코드 작성 순서가 INSERT → UPDATE 인 자연스러운 케이스.
         * 테스트 1과 결과가 동일해야 한다.
         *
         * 예상 SQL 로그:
         *   insert into test_user (name) values ('신규유저')
         *   update test_user set name='수정된유저' where id=?
         */

        // INSERT 대상 설정 (코드 상 먼저)
        em.persist(new TestUser("신규유저"));

        // UPDATE 대상 설정 (코드 상 나중)
        TestUser existing = em.find(TestUser.class, existingId);
        existing.setName("수정된유저");

        em.flush();     // ← 여기서 SQL 로그 확인: INSERT(신규유저) 가 UPDATE(수정된유저) 보다 먼저 나와야 함
    }
}