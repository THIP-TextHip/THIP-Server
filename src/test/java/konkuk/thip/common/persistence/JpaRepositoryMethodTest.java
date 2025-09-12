package konkuk.thip.common.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;

@DataJpaTest
@ActiveProfiles("test")
@EnableJpaRepositories(
        considerNestedRepositories = true,
        basePackageClasses = JpaRepositoryMethodTest.class // 이 테스트 안의 nested repo만 스캔
)
@EntityScan(basePackageClasses = JpaRepositoryMethodTest.class)
public class JpaRepositoryMethodTest {

    /**
     * JPA 리포지토리의 PK 조회(findById)와 파생 쿼리(JPQL 기반, findByUserId)의 내부 동작 차이를
     * “영속성 컨텍스트(1차 캐시), auto flush, 동일성 해석(Identity Resolution)”
     * 관점에서 검증하는 슬라이스 테스트입니다.
     */

    @Autowired EntityManager em;
    @Autowired TestUserRepository testUserRepository;

    @Entity
    @Getter
    @Setter
    @NoArgsConstructor
    private static class TestUser {
        @Id
        @GeneratedValue
        private Long userId;

        private String nickname;

        public TestUser(String nickname) {
            this.nickname = nickname;
        }
    }

    private interface TestUserRepository extends JpaRepository<TestUser, Long> {
        Optional<TestUser> findByUserId(Long userId);
    }

    @Test
    @DisplayName("jpa repository가 제공하는 findById 메서드는 영속성 컨텍스트 flush 또는 쿼리 없이 1차 캐시를 먼저 바라본다.")
    void findById_use_first_level_cache_without_flush_or_query() throws Exception {
        //given
        TestUser testUser = new TestUser("노성준");
        em.persist(testUser);      // 1차 캐시에 등록 (아직 DB에 반영되지 않음, flush X)
        Long id = testUser.getUserId();

        //when
        TestUser found = testUserRepository.findById(id).orElseThrow();     // findById 메서드 호출

        //then
        assertSame(testUser, found);       // 동일 인스턴스임을 확인 -> 1차 캐시에 저장된 엔티티를 조회했으므로
        /**
         * 추가로 로그에서 select 쿼리가 실행되지 않았음을 확인할 수 있다.
         * insert 쿼리는 트랜잭션 커밋 -> flush 시점에 실행된다.
         */
    }

    @Test
    @DisplayName("jpa repository에 정의한 jpql 메서드는 필요시 auto flush -> DB query -> 영속성 컨텍스트에서의 동일성 해석 과정을 거친 후 엔티티를 반환한다.")
    void derivedQuery_auto_flush_if_needed_then_select_and_identity_resolution() throws Exception {
        //given
        TestUser testUser = new TestUser("노성준");
        em.persist(testUser);      // 1차 캐시에 등록 (아직 DB에 반영되지 않음, flush X)
        Long id = testUser.getUserId();

        testUser.setNickname("김희용");    // 더티(수정) 상태

        //when
        TestUser found = testUserRepository.findByUserId(id).orElseThrow();     // findByUserId 메서드 호출
        /**
         * 이때 FlushMode = AUTO(디폴트) 이므로, hibernate는 쿼리 실행 전 auto flush 필요 여부를 판단함
         * (-> 변경 사항이 현재 쿼리 결과에 영향을 줄 수 있는지를 판단)
         * 이후 DB에 query가 날라간다
         */

        //then
        assertSame(testUser, found);       // 동일 인스턴스임을 확인 -> DB select 쿼리가 나가지만, 동일성 해석을 거쳐 1차 캐시의 인스턴스를 반환
        /**
         * 이미 1차 캐시에 DB에서 로드한 엔티티와 동일한 키값을 가지는 엔티티가 존재하므로, 기존 인스턴스를 반환한다.
         */
    }

    @Test
    @DisplayName("영속성 컨텍스트 내부에서 remove 처리된 엔티티를 findById 메서드로 조회할 경우, null을 반환한다.")
    void findById_returns_null_if_entity_removed_in_first_level_cache() throws Exception {
        //given
        TestUser testUser = new TestUser("노성준");
        em.persist(testUser);
        Long id = testUser.getUserId();

        em.remove(testUser);    // 영속성 컨텍스트에서 removed로 마킹

        //when //then
        assertThat(testUserRepository.findById(id)).isEmpty();
    }
}
