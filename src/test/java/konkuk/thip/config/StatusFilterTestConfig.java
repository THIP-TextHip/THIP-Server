package konkuk.thip.config;

import konkuk.thip.common.annotation.persistence.Unfiltered;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Configuration
public class StatusFilterTestConfig {

    /**
     * 테스트용 영속성 Adapter.
     * 실제 *QueryPersistenceAdapter 와 동일하게 class-level @Transactional(readOnly=true) 를 가진다.
     * adapter 경계에서 filter 가 활성화되므로, 내부 구현(JPA repo / QueryDSL)과 무관하게 filter 가 적용된다.
     */
    @Transactional(readOnly = true)
    @Repository
    @RequiredArgsConstructor
    public static class TestUserPersistenceAdapter {

        private final TestUserJpaRepository testUserJpaRepository;

        public Optional<UserJpaEntity> findByUserId(Long userId) {
            return testUserJpaRepository.findByUserId(userId);
        }

        public List<UserJpaEntity> findAll() {
            return testUserJpaRepository.findAll();
        }
    }

    /**
     * @Unfiltered 동작 검증용 서비스.
     * service 메서드에서 @Transactional 유무와 관계없이 filter 가 비활성화됨을 검증한다.
     */
    @Component
    @RequiredArgsConstructor
    public static class TestUnfilteredService {

        private final TestUserPersistenceAdapter testUserPersistenceAdapter;

        /** @Unfiltered + @Transactional: filter 비활성화 */
        @Unfiltered
        @Transactional(readOnly = true)
        public List<UserJpaEntity> findAllWithTx() {
            return testUserPersistenceAdapter.findAll();
        }

        /** @Unfiltered only (no @Transactional): filter 비활성화 */
        @Unfiltered
        public List<UserJpaEntity> findAllWithoutTx() {
            return testUserPersistenceAdapter.findAll();
        }
    }
}
