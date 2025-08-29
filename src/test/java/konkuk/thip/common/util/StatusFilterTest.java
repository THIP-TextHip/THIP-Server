package konkuk.thip.common.util;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.book.adapter.out.persistence.repository.SavedBookJpaRepository;
import konkuk.thip.config.StatusFilterTestConfig;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.domain.value.Alias;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class StatusFilterTest {

    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private BookJpaRepository bookJpaRepository;
    @Autowired private SavedBookJpaRepository savedBookJpaRepository;

    @Autowired private StatusFilterTestConfig.TestUserIdFindService testUserIdFindService;
    @Autowired private StatusFilterTestConfig.TestUserQueryService testUserQueryService;
    @Autowired private StatusFilterTestConfig.TestUserJpqlService testUserJpqlService;
    @Autowired private StatusFilterTestConfig.TestUserQuerydslService testUserQuerydslService;

    @Autowired private JdbcTemplate jdbcTemplate;

    @AfterEach
    public void tearDown() {
        savedBookJpaRepository.deleteAllInBatch();
        bookJpaRepository.deleteAllInBatch();
        userJpaRepository.deleteAllInBatch();
    }

    private void saveActiveUser(int count) {
        for (int i = 1; i <= count; i++) {
            UserJpaEntity user = TestEntityFactory.createUser(Alias.WRITER, "activeUser" + i);
            userJpaRepository.save(user);
        }
    }

    private void saveInactiveUser(int count) {
        for (int i = 1; i <= count; i++) {
            UserJpaEntity user = TestEntityFactory.createUser(Alias.WRITER, "inactiveUser" + i);
            userJpaRepository.save(user);

            jdbcTemplate.update(
                    "UPDATE users SET status = 'INACTIVE' WHERE user_id = ?",
                    user.getUserId()
            );
        }
    }

    @Test
    @DisplayName("spring data jpa의 기본 findById 메서드는 PK를 기준으로만 조회하므로 status 필터링이 적용되지 않는다.")
    @Transactional  // filter를 활성화 하기 위한 트랜잭션 어노테이션
    void default_find_by_id_method_does_not_execute_filtering() throws Exception {
        //given
        UserJpaEntity activeUser = userJpaRepository.save(TestEntityFactory.createUser(Alias.WRITER, "activeUser"));
        UserJpaEntity inactiveUser = userJpaRepository.save(TestEntityFactory.createUser(Alias.WRITER, "inactiveUser"));
        jdbcTemplate.update(
                "UPDATE users SET status = 'INACTIVE' WHERE user_id = ?",
                inactiveUser.getUserId()
        );

        //when
        Optional<UserJpaEntity> findActiveUser = testUserIdFindService.defaultFindById(activeUser.getUserId());
        Optional<UserJpaEntity> findInactiveUser = testUserIdFindService.defaultFindById(inactiveUser.getUserId());

        //then
        assertThat(findActiveUser).isPresent();
        assertThat(findInactiveUser).isPresent();   // status 필터링이 적용되지 않아서 INACTIVE 엔티티도 조회됨
    }

    @Test
    @DisplayName("jpa repository에 정의한 custom 메서드는 status 필터링이 적용된다.")
    @Transactional  // filter를 활성화 하기 위한 트랜잭션 어노테이션
    void custom_find_active_by_id_method_does_execute_filtering() throws Exception {
        //given
        UserJpaEntity activeUser = userJpaRepository.save(TestEntityFactory.createUser(Alias.WRITER, "activeUser"));
        UserJpaEntity inactiveUser = userJpaRepository.save(TestEntityFactory.createUser(Alias.WRITER, "inactiveUser"));
        jdbcTemplate.update(
                "UPDATE users SET status = 'INACTIVE' WHERE user_id = ?",
                inactiveUser.getUserId()
        );

        //when
        Optional<UserJpaEntity> findActiveUser = testUserIdFindService.customFindById(activeUser.getUserId());
        Optional<UserJpaEntity> findInactiveUser = testUserIdFindService.customFindById(inactiveUser.getUserId());

        //then
        assertThat(findActiveUser).isPresent();
        assertThat(findInactiveUser).isNotPresent();   // status 필터링이 적용되어 INACTIVE 엔티티는 조회되지 않음
    }

    @Test
    @DisplayName("[jpa 쿼리 메서드] active 상태인 엔티티만 조회하는 것이 기본 동작이다.")
    void jpa_query_method_default_find_active_entities() throws Exception {
        //given
        saveActiveUser(3);
        saveInactiveUser(2);

        //when
        List<UserJpaEntity> userJpaEntities = testUserQueryService.findAllActiveOnly();

        //then
        assertThat(userJpaEntities).hasSize(3)
                .extracting(UserJpaEntity::getNickname)
                .containsExactlyInAnyOrder(
                        "activeUser1", "activeUser2", "activeUser3"
                );
    }

    @Test
    @DisplayName("[jpa 쿼리 메서드] IncludeInactive 어노테이션이 붙은 메서드는 active, inactive 상태인 모든 엔티티를 조회한다.")
    void jpa_query_method_specific_find_active_and_inactive_entities() throws Exception {
        //given
        saveActiveUser(3);
        saveInactiveUser(2);

        //when
        List<UserJpaEntity> userJpaEntities = testUserQueryService.findAllIncludingInactive();

        //then
        assertThat(userJpaEntities).hasSize(5)
                .extracting(UserJpaEntity::getNickname)
                .containsExactlyInAnyOrder(
                        "activeUser1", "activeUser2", "activeUser3", "inactiveUser1", "inactiveUser2"
                );
    }

    @Test
    @DisplayName("[jpql] active 상태인 엔티티만 조회하는 것이 기본 동작이다.")
    void jpql_default_find_active_entities() throws Exception {
        //given
        saveActiveUser(3);
        saveInactiveUser(2);

        //when
        List<UserJpaEntity> userJpaEntities = testUserJpqlService.findAllByJpql();

        //then
        assertThat(userJpaEntities).hasSize(3)
                .extracting(UserJpaEntity::getNickname)
                .containsExactlyInAnyOrder(
                        "activeUser1", "activeUser2", "activeUser3"
                );
    }

    @Test
    @DisplayName("[jpql] IncludeInactive 어노테이션이 붙은 메서드는 active, inactive 상태인 모든 엔티티를 조회한다.")
    void jpql_specific_find_active_and_inactive_entities() throws Exception {
        //given
        saveActiveUser(3);
        saveInactiveUser(2);

        //when
        List<UserJpaEntity> userJpaEntities = testUserJpqlService.findAllIncludingInactiveByJpql();

        //then
        assertThat(userJpaEntities).hasSize(5)
                .extracting(UserJpaEntity::getNickname)
                .containsExactlyInAnyOrder(
                        "activeUser1", "activeUser2", "activeUser3", "inactiveUser1", "inactiveUser2"
                );
    }

    @Test
    @DisplayName("[querydsl] active 상태인 엔티티만 조회하는 것이 기본 동작이다.")
    void query_dsl_default_find_active_entities() throws Exception {
        //given
        saveActiveUser(3);
        saveInactiveUser(2);

        //when
        List<UserJpaEntity> userJpaEntities = testUserQuerydslService.findAllByQuerydsl();

        //then
        assertThat(userJpaEntities).hasSize(3)
                .extracting(UserJpaEntity::getNickname)
                .containsExactlyInAnyOrder(
                        "activeUser1", "activeUser2", "activeUser3"
                );
    }

    @Test
    @DisplayName("[querydsl 쿼리 메서드] IncludeInactive 어노테이션이 붙은 메서드는 active, inactive 상태인 모든 엔티티를 조회한다.")
    void query_dsl_specific_find_active_and_inactive_entities() throws Exception {
        //given
        saveActiveUser(3);
        saveInactiveUser(2);

        //when
        List<UserJpaEntity> userJpaEntities = testUserQuerydslService.findAllIncludingInactiveByQuerydsl();

        //then
        assertThat(userJpaEntities).hasSize(5)
                .extracting(UserJpaEntity::getNickname)
                .containsExactlyInAnyOrder(
                        "activeUser1", "activeUser2", "activeUser3", "inactiveUser1", "inactiveUser2"
                );
    }

    @Test
    @DisplayName("[join 테스트] 루트=User + SavedBook ON-조인 시: 기본은 ACTIVE만, @IncludeInactive 적용 시 ACTIVE+INACTIVE 모두 집계한다.")
    void join_filter_propagation_on_user() throws Exception {
        //given
        UserJpaEntity activeUser = userJpaRepository.save(TestEntityFactory.createUser(Alias.WRITER, "activeUser"));
        UserJpaEntity inactiveUser = userJpaRepository.save(TestEntityFactory.createUser(Alias.WRITER, "inactiveUser"));
        jdbcTemplate.update(
                "UPDATE users SET status = 'INACTIVE' WHERE user_id = ?",
                inactiveUser.getUserId()
        );

        BookJpaEntity bookJpaEntity = bookJpaRepository.save(TestEntityFactory.createBook());

        savedBookJpaRepository.save(TestEntityFactory.createSavedBook(activeUser, bookJpaEntity));
        savedBookJpaRepository.save(TestEntityFactory.createSavedBook(inactiveUser, bookJpaEntity));

        //when
        long defCount = testUserQuerydslService.countSaversByBook(bookJpaEntity.getBookId());
        long incCount = testUserQuerydslService.countSaversByBookIncludingInactive(bookJpaEntity.getBookId());

        //then
        assertThat(defCount).isEqualTo(1);  // active user만 카운트
        assertThat(incCount).isEqualTo(2);  // active + inactive user 모두 카운트
    }
}
