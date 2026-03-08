package konkuk.thip.common.persistence;

import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.config.StatusFilterTestConfig;
import konkuk.thip.config.TestUserJpaRepository;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.domain.value.Alias;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Hibernate Filter (statusFilter) 동작 검증 테스트.
 *
 * 검증 범위:
 * 1. findById 는 PK 조회로 Hibernate filter 가 적용되지 않는다.
 * 2. @Transactional(readOnly=true) 가 명시된 adapter 경계에서 filter 가 활성화된다.
 * 3. adapter 내부 구현(JPA repo)과 무관하게 filter 가 동작한다.
 * 4. service 메서드에 @Unfiltered 가 있으면 @Transactional 유무와 관계없이 filter 가 비활성화된다.
 */
@SpringBootTest
@ActiveProfiles("test")
public class StatusFilterTest {

    @Autowired private TestUserJpaRepository testUserJpaRepository;
    @Autowired private StatusFilterTestConfig.TestUserPersistenceAdapter testUserPersistenceAdapter;
    @Autowired private StatusFilterTestConfig.TestUnfilteredService testUnfilteredService;

    @Autowired private JdbcTemplate jdbcTemplate;

    @AfterEach
    void cleanup() {
        jdbcTemplate.update("DELETE FROM users");
    }

    private UserJpaEntity saveActiveUser(String nickname) {
        return testUserJpaRepository.save(TestEntityFactory.createUser(Alias.WRITER, nickname));
    }

    private UserJpaEntity saveInactiveUser(String nickname) {
        UserJpaEntity user = testUserJpaRepository.save(TestEntityFactory.createUser(Alias.WRITER, nickname));
        jdbcTemplate.update("UPDATE users SET status = 'INACTIVE' WHERE user_id = ?", user.getUserId());
        return user;
    }

    @Test
    @DisplayName("findById 는 PK 조회이므로 Hibernate filter 가 적용되지 않아 INACTIVE 엔티티도 조회된다.")
    void findById_bypasses_hibernate_filter() {
        //given
        UserJpaEntity activeUser = saveActiveUser("activeUser");
        UserJpaEntity inactiveUser = saveInactiveUser("inactiveUser");

        //when
        Optional<UserJpaEntity> findActive = testUserJpaRepository.findById(activeUser.getUserId());
        Optional<UserJpaEntity> findInactive = testUserJpaRepository.findById(inactiveUser.getUserId());

        //then
        assertThat(findActive).isPresent();
        assertThat(findInactive).isPresent();   // PK 조회라 filter 미적용 → INACTIVE 도 조회됨
    }

    @Test
    @DisplayName("adapter 의 @Transactional(readOnly=true) 경계에서 filter 가 활성화되어 ACTIVE 엔티티만 조회된다.")
    void adapter_transactional_applies_filter() {
        //given
        saveActiveUser("activeUser1");
        saveActiveUser("activeUser2");
        saveInactiveUser("inactiveUser1");

        //when
        List<UserJpaEntity> users = testUserPersistenceAdapter.findAll();

        //then
        assertThat(users).hasSize(2)
                .extracting(UserJpaEntity::getNickname)
                .containsExactlyInAnyOrder("activeUser1", "activeUser2");
    }

    @Test
    @DisplayName("adapter 내부에서 JPQL 기반 메서드를 호출해도 filter 가 적용되어 INACTIVE 엔티티는 조회되지 않는다.")
    void adapter_jpql_method_applies_filter() {
        //given
        UserJpaEntity activeUser = saveActiveUser("activeUser");
        UserJpaEntity inactiveUser = saveInactiveUser("inactiveUser");

        //when
        Optional<UserJpaEntity> findActive = testUserPersistenceAdapter.findByUserId(activeUser.getUserId());
        Optional<UserJpaEntity> findInactive = testUserPersistenceAdapter.findByUserId(inactiveUser.getUserId());

        //then
        assertThat(findActive).isPresent();
        assertThat(findInactive).isNotPresent();    // filter 적용 → INACTIVE 조회 안 됨
    }

    @Test
    @DisplayName("service 메서드에 @Unfiltered + @Transactional 이 함께 있으면 filter 가 비활성화되어 모든 엔티티가 조회된다.")
    void unfiltered_with_transactional_disables_filter() {
        //given
        saveActiveUser("activeUser1");
        saveActiveUser("activeUser2");
        saveInactiveUser("inactiveUser1");

        //when
        List<UserJpaEntity> users = testUnfilteredService.findAllWithTx();

        //then
        assertThat(users).hasSize(3)
                .extracting(UserJpaEntity::getNickname)
                .containsExactlyInAnyOrder("activeUser1", "activeUser2", "inactiveUser1");
    }

    @Test
    @DisplayName("service 메서드에 @Transactional 없이 @Unfiltered 만 있어도 filter 가 비활성화되어 모든 엔티티가 조회된다.")
    void unfiltered_without_transactional_disables_filter() {
        //given
        saveActiveUser("activeUser1");
        saveActiveUser("activeUser2");
        saveInactiveUser("inactiveUser1");

        //when
        List<UserJpaEntity> users = testUnfilteredService.findAllWithoutTx();

        //then
        assertThat(users).hasSize(3)
                .extracting(UserJpaEntity::getNickname)
                .containsExactlyInAnyOrder("activeUser1", "activeUser2", "inactiveUser1");
    }
}
