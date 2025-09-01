package konkuk.thip.common.aop;

import jakarta.persistence.EntityManager;
import konkuk.thip.common.entity.StatusType;
import konkuk.thip.common.exception.InvalidStateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

import static konkuk.thip.common.exception.code.ErrorCode.PERSISTENCE_TRANSACTION_REQUIRED;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class StatusFilterAspect {

    private final EntityManager em;

    /**
     * Hibernate Session은 thread-not-safe 하므로 반드시 트랜잭션 경계 내에서만 사용해야함
     * 현재 스레드에 바인딩된 EntityManager를 통해 세션을 획득하도록 강제
     */
    private Session currentTxSession() {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new InvalidStateException(PERSISTENCE_TRANSACTION_REQUIRED);
        }
        return session();
    }

    private Session session() {
        return em.unwrap(Session.class);    // 현재 스레드의 em에서 Hibernate 세션 얻기
    }

    private static final String FILTER_NAME = "statusFilter";
    private static final String PARAM_STATUSES = "statuses";

    private static final String ANN_TX = "org.springframework.transaction.annotation.Transactional";
    private static final String ANN_INCLUDE_INACTIVE = "konkuk.thip.common.annotation.persistence.IncludeInactive";
    private static final String ANN_UNFILTERED = "konkuk.thip.common.annotation.persistence.Unfiltered";

    // 기본: ACTIVE만 (트랜잭션 경계 진입 시)
    // 1) @Transactional 이고
    // 2) @IncludeInactive, @Unfiltered 가 붙어있지 않은 경우에만 적용
    private static final String PCUT_TX_DEFAULT =
            "(" + "@annotation(" + ANN_TX + ") || @within(" + ANN_TX + ")" + ")" +
                    " && !" + "@annotation(" + ANN_INCLUDE_INACTIVE + ")" +
                    " && !" + "@annotation(" + ANN_UNFILTERED + ")";

    // @IncludeInactive: 트랜잭션 컨텍스트가 보장된 경우에만 동작
    private static final String PCUT_INCLUDE_INACTIVE =
            "@annotation(" + ANN_INCLUDE_INACTIVE + ") && (" + "@annotation(" + ANN_TX + ") || @within(" + ANN_TX + ")" + ")";

    // @Unfiltered: 트랜잭션 컨텍스트가 보장된 경우에만 동작
    private static final String PCUT_UNFILTERED =
            "@annotation(" + ANN_UNFILTERED + ") && (" + "@annotation(" + ANN_TX + ") || @within(" + ANN_TX + ")" + ")";

    // 기본: ACTIVE만
    @Around(PCUT_TX_DEFAULT)
    public Object enableActiveByDefault(ProceedingJoinPoint pjp) throws Throwable {
        var s = currentTxSession();
        var wasEnabled = isFilterEnabled(s);
        if (!wasEnabled) {
            enableFilterWith(s, List.of(StatusType.ACTIVE.name()));
        }
        try {
            return pjp.proceed();
        } finally {
            if (!wasEnabled) {
                disableFilter(s);
            }
        }
    }

    // Include Inactive: ACTIVE, INACTIVE 모두 + 종료 시 active-only 로 복귀
    @Around(PCUT_INCLUDE_INACTIVE)
    public Object includeInactive(ProceedingJoinPoint pjp) throws Throwable {
        var s = currentTxSession();
        var prevEnabled = isFilterEnabled(s);

        enableFilterWith(s, List.of(StatusType.ACTIVE.name(), StatusType.INACTIVE.name()));

        try {
            return pjp.proceed();
        } finally {
            restoreToActive(s);
            if (!prevEnabled) {
                disableFilter(s);
            }
        }
    }

    // Unfiltered: 필터 해제 + 종료 시 active-only 로 복귀
    @Around(PCUT_UNFILTERED)
    public Object unfiltered(ProceedingJoinPoint pjp) throws Throwable {
        var s = currentTxSession();
        var wasEnabled = isFilterEnabled(s);
        if (wasEnabled) {
            disableFilter(s);
        }
        try {
            return pjp.proceed();
        } finally {
            if (wasEnabled) {
                restoreToActive(s);
            }
        }
    }

    private boolean isFilterEnabled(Session s) {
        return s.getEnabledFilter(FILTER_NAME) != null;
    }

    private void enableFilterWith(Session s, List<String> statuses) {
        s.enableFilter(FILTER_NAME).setParameterList(PARAM_STATUSES, statuses);
        log.debug("statusFilter -> ENABLED [statuses={}]", statuses);
    }

    private void restoreToActive(Session s) {
        var restored = List.of(StatusType.ACTIVE.name());
        s.enableFilter(FILTER_NAME).setParameterList(PARAM_STATUSES, restored);
        log.debug("statusFilter -> RESTORED [statuses={}]", restored);
    }

    private void disableFilter(Session s) {
        s.disableFilter(FILTER_NAME);
        log.debug("statusFilter -> DISABLED");
    }
}
