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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class StatusFilterTest {

    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private BookJpaRepository bookJpaRepository;
    @Autowired private SavedBookJpaRepository savedBookJpaRepository;

    @Autowired private StatusFilterTestConfig.TestUserService testUserService;
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
    @DisplayName("[jpa 쿼리 메서드] active 상태인 엔티티만 조회하는 것이 기본 동작이다.")
    void jpa_query_method_default_find_active_entities() throws Exception {
        //given
        saveActiveUser(3);
        saveInactiveUser(2);

        //when
        List<UserJpaEntity> userJpaEntities = testUserService.findAllActiveOnly();

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
        List<UserJpaEntity> userJpaEntities = testUserService.findAllIncludingInactive();

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

//    @Test
//    @DisplayName("LEFT JOIN + 글로벌 필터 ON: 자식이 INACTIVE뿐이면 부모가 사라져 count=0, 우회 방법은 count=1")
//    void leftJoinBehavesInnerWhenAllChildrenInactive_thenBypassKeepsParent() {
//        // given: Book 1권, SavedBook 1건(자식) — 자식은 INACTIVE로 강제
//        UserJpaEntity u = userJpaRepository.save(TestEntityFactory.createUser(Alias.WRITER, "u1"));
//        BookJpaEntity b = bookJpaRepository.save(TestEntityFactory.createBook());
//        var sb = savedBookJpaRepository.save(TestEntityFactory.createSavedBook(u, b));
//        jdbcTemplate.update("UPDATE saved_books SET status = 'INACTIVE' WHERE saved_id = ?", sb.getSavedId());
//
//        // when
//        long defaultCount = testLeftJoinQuerydslService.countBooksWithLeftJoinDefault(b.getBookId());          // 글로벌 필터 ON (WHERE에 sb.status 조건 주입)
//        long onClauseCount = testLeftJoinQuerydslService.countBooksWithLeftJoinOnActive(b.getBookId());        // 필터 OFF + ON절로 ACTIVE 조건
//
//        // then
//        assertThat(defaultCount).isZero(); // 부모(Book)는 있지만, WHERE에 sb 조건이 들어가면서 행이 사라짐 → 0
//        assertThat(onClauseCount).isEqualTo(1L); // 부모 보존(LEFT 의미 유지) → 1
//    }
//
//    @Test
//    @DisplayName("LEFT JOIN + 글로벌 필터 ON: ACTIVE 자식 2개면 count=2(중복), 우회 방법은 부모 기준으로 count=1")
//    void leftJoinDuplicatesWithMultipleActiveChildren_thenBypassDedupToParent() {
//        // given: Book 1권 + ACTIVE SavedBook 2건
//        UserJpaEntity u = userJpaRepository.save(TestEntityFactory.createUser(Alias.WRITER, "u1"));
//        BookJpaEntity b = bookJpaRepository.save(TestEntityFactory.createBook());
//        savedBookJpaRepository.save(TestEntityFactory.createSavedBook(u, b));
//        savedBookJpaRepository.save(TestEntityFactory.createSavedBook(u, b));
//
//        // when
//        long defaultCount = testLeftJoinQuerydslService.countBooksWithLeftJoinDefault(b.getBookId());   // WHERE에 sb.status=ACTIVE → 부모 중복 발생 → 2
//        long onClauseCount = testLeftJoinQuerydslService.countBooksWithLeftJoinOnActive(b.getBookId()); // ON절에 ACTIVE 조건, 부모 보존 관점에서 → 1
//
//        // then
//        assertThat(defaultCount).isEqualTo(2L); // 자식 2건으로 인해 조인 곱 2건
//        assertThat(onClauseCount).isEqualTo(1L); // 부모 기준 1건
//    }
}
