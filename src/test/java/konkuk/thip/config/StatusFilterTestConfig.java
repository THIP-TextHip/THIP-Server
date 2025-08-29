package konkuk.thip.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import konkuk.thip.book.adapter.out.jpa.QSavedBookJpaEntity;
import konkuk.thip.common.annotation.persistence.IncludeInactive;
import konkuk.thip.user.adapter.out.jpa.QUserJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Configuration
public class StatusFilterTestConfig {

    // jpa query 메서드
    @Component
    @RequiredArgsConstructor
    public static class TestUserService {

        private final UserJpaRepository userJpaRepository;

        /** 기본: ACTIVE만 (Aspect가 트랜잭션 경계에서 statusFilter를 ACTIVE로 enable) */
        @Transactional(readOnly = true)
        public List<UserJpaEntity> findAllActiveOnly() {
            return userJpaRepository.findAll();
        }

        /** IncludeInactive: ACTIVE + INACTIVE 모두 */
        @IncludeInactive
        @Transactional(readOnly = true)
        public List<UserJpaEntity> findAllIncludingInactive() {
            return userJpaRepository.findAll();
        }
    }

    // jpql
    @Component
    @RequiredArgsConstructor
    public static class TestUserJpqlService {
        private final EntityManager em;

        /** 기본: ACTIVE만 */
        @Transactional(readOnly = true)
        public List<UserJpaEntity> findAllByJpql() {
            return em.createQuery(
                    "select u from UserJpaEntity u", UserJpaEntity.class
            ).getResultList();
        }

        /** IncludeInactive: ACTIVE + INACTIVE */
        @IncludeInactive
        @Transactional(readOnly = true)
        public List<UserJpaEntity> findAllIncludingInactiveByJpql() {
            return em.createQuery(
                    "select u from UserJpaEntity u", UserJpaEntity.class
            ).getResultList();
        }
    }

    // querydsl
    @Component
    @RequiredArgsConstructor
    public static class TestUserQuerydslService {
        private final JPAQueryFactory qf;

        /** 기본: ACTIVE만 */
        @Transactional(readOnly = true)
        public List<UserJpaEntity> findAllByQuerydsl() {
            QUserJpaEntity u = QUserJpaEntity.userJpaEntity;
            return qf.selectFrom(u).fetch();
        }

        /** IncludeInactive: ACTIVE + INACTIVE */
        @IncludeInactive
        @Transactional(readOnly = true)
        public List<UserJpaEntity> findAllIncludingInactiveByQuerydsl() {
            QUserJpaEntity u = QUserJpaEntity.userJpaEntity;
            return qf.selectFrom(u).fetch();
        }

        @Transactional(readOnly = true)
        public long countSaversByBook(Long bookId) {
            QSavedBookJpaEntity sb = QSavedBookJpaEntity.savedBookJpaEntity;
            QUserJpaEntity u = QUserJpaEntity.userJpaEntity;
            return qf.select(u.userId.countDistinct())
                    .from(u)
                    .join(sb).on(sb.userJpaEntity.eq(u))
                    .where(sb.bookJpaEntity.bookId.eq(bookId))
                    .fetchOne();
        }

        @IncludeInactive
        @Transactional(readOnly = true)
        public long countSaversByBookIncludingInactive(Long bookId) {
            QSavedBookJpaEntity sb = QSavedBookJpaEntity.savedBookJpaEntity;
            QUserJpaEntity u = QUserJpaEntity.userJpaEntity;
            return qf.select(u.userId.countDistinct())
                    .from(u)
                    .join(sb).on(sb.userJpaEntity.eq(u))
                    .where(sb.bookJpaEntity.bookId.eq(bookId))
                    .fetchOne();
        }
    }
}
