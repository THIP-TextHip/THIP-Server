package konkuk.thip.common.aop;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import static konkuk.thip.common.aop.FilterContextHolder.FilterMode.ACTIVE_ONLY;
import static konkuk.thip.common.aop.FilterContextHolder.FilterMode.UNFILTERED;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class StatusFilterAspect {

    private final EntityManager em;

    private static final String FILTER_NAME = "statusFilter";

    private static final String ANN_TX = "org.springframework.transaction.annotation.Transactional";
    private static final String ANN_UNFILTERED = "konkuk.thip.common.annotation.persistence.Unfiltered";

    /**
     * @Unfiltered: @Transactional 없이도 동작.
     * ThreadLocal에 UNFILTERED 의도를 기록하고 반환.
     * 실제 Session 조작은 트랜잭션 경계(PCUT_TX_DEFAULT)에서 수행.
     */
    private static final String PCUT_UNFILTERED = "@annotation(" + ANN_UNFILTERED + ")";

    /**
     * 기본: @Transactional 경계에서 ThreadLocal을 읽어 filter를 활성화.
     * @Unfiltered가 붙은 메서드는 제외 (PCUT_UNFILTERED에서 처리).
     */
    private static final String PCUT_TX_DEFAULT =
            "(" + "@annotation(" + ANN_TX + ") || @within(" + ANN_TX + ")" + ")" +
                    " && !" + "@annotation(" + ANN_UNFILTERED + ")";

    @Around(PCUT_UNFILTERED)
    public Object unfiltered(ProceedingJoinPoint pjp) throws Throwable {
        FilterContextHolder.FilterMode prev = FilterContextHolder.get();
        FilterContextHolder.set(UNFILTERED);
        try {
            return pjp.proceed();
        } finally {
            FilterContextHolder.set(prev);
        }
    }

    @Around(PCUT_TX_DEFAULT)
    public Object enableActiveByDefault(ProceedingJoinPoint pjp) throws Throwable {
        Session s = em.unwrap(Session.class);
        boolean wasEnabled = isFilterEnabled(s);

        if (FilterContextHolder.get() == ACTIVE_ONLY && !wasEnabled) {
            enableFilter(s);
        }

        try {
            return pjp.proceed();
        } finally {
            if (!wasEnabled) {
                disableFilter(s);
            }
        }
    }

    private boolean isFilterEnabled(Session s) {
        return s.getEnabledFilter(FILTER_NAME) != null;
    }

    private void enableFilter(Session s) {
        s.enableFilter(FILTER_NAME);
        log.debug("statusFilter -> ENABLED [ACTIVE only]");
    }

    private void disableFilter(Session s) {
        s.disableFilter(FILTER_NAME);
        log.debug("statusFilter -> DISABLED");
    }
}