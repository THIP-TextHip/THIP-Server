package konkuk.thip.notification.application.service;

import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.message.adapter.out.event.dto.FeedEvents;
import konkuk.thip.message.application.port.in.FeedNotificationDispatchUseCase;
import konkuk.thip.notification.adapter.out.jpa.NotificationJpaEntity;
import konkuk.thip.notification.adapter.out.persistence.repository.NotificationJpaRepository;
import konkuk.thip.notification.application.port.in.FeedNotificationOrchestrator;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.domain.value.Alias;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("[통합] 피드 알림 (동기화 방식) 헬퍼 서비스 통합 테스트")
class FeedNotificationOrchestratorSyncImplTest {

    @Autowired FeedNotificationOrchestrator orchestrator; // 프록시를 타기 위해 인터페이스 타입 주입
    @Autowired NotificationJpaRepository notificationJpaRepository;
    @Autowired UserJpaRepository userJpaRepository;

    @MockitoBean FeedNotificationDispatchUseCase feedNotificationDispatchUseCase;

    private Long targetUserId;

    @BeforeEach
    void setUp() {
        // Notification 저장 시 FK 검사 통과를 위해 대상 유저 하나 만들어 둠
        UserJpaEntity target = userJpaRepository.save(TestEntityFactory.createUser(Alias.WRITER, "노성준"));
        targetUserId = target.getUserId();
    }

    @AfterEach
    void tearDown() {
        // 롤백이 아닌 경우를 대비한 안전 정리
        notificationJpaRepository.deleteAllInBatch();
        userJpaRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("상위 트랜잭션 없이 호출하면 IllegalTransactionStateException 발생 (MANDATORY)")
    void mandatory_without_transaction_throws() {
        // when & then
        assertThatThrownBy(() ->
                orchestrator.notifyFeedCommented(
                        targetUserId, /*actor*/ 999L, "alice", /*feedId*/ 123L
                )
        ).isInstanceOf(IllegalTransactionStateException.class);
    }

    @Test
    @Transactional
    @DisplayName("상위 트랜잭션 안에서 호출하면 정상 동작하고, Notification이 저장된다")
    void mandatory_with_transaction_succeeds_and_persists() {
        // when
        orchestrator.notifyFeedCommented(
                targetUserId, /*actor*/ 1000L, "bob", /*feedId*/ 777L
        );

        // then (같은 트랜잭션 안에서 즉시 조회 가능)
        var all = notificationJpaRepository.findAll();
        assertThat(all).hasSize(1);

        NotificationJpaEntity saved = all.get(0);
        assertThat(saved.getUserJpaEntity().getUserId()).isEqualTo(targetUserId);
        assertThat(saved.getTitle()).isNotBlank();
        assertThat(saved.getContent()).contains("bob");
    }

    @Test
    @Transactional
    @DisplayName("커밋 시: AFTER_COMMIT 리스너가 handleFeedCommented 호출 & Notification 커밋됨")
    void notifyFeedCommented_afterCommit_listenerInvoked_andNotificationPersisted() {
        // given
        Long actorUserId = 200L;
        String actorUsername = "alice";
        Long feedId = 999L;

        // when (트랜잭션 안)
        orchestrator.notifyFeedCommented(targetUserId, actorUserId, actorUsername, feedId);

        // 실제 커밋 트리거
        TestTransaction.flagForCommit();
        TestTransaction.end(); // 여기서 @TransactionalEventListener(AFTER_COMMIT) 실행됨 (테스트 프로필은 동기 실행)

        // then : 리스너에 전달되는 DTO 필드 검증
        ArgumentCaptor<FeedEvents.FeedCommentedEvent> captor =
                ArgumentCaptor.forClass(FeedEvents.FeedCommentedEvent.class);
        verify(feedNotificationDispatchUseCase).handleFeedCommented(captor.capture());

        FeedEvents.FeedCommentedEvent event = captor.getValue();
        assertThat(event).isNotNull();
        assertThat(event.title()).isNotBlank();
        assertThat(event.content()).contains(actorUsername);
        assertThat(event.targetUserId()).isEqualTo(targetUserId);
        assertThat(event.actorUserId()).isEqualTo(actorUserId);
        assertThat(event.actorUsername()).isEqualTo(actorUsername);
        assertThat(event.feedId()).isEqualTo(feedId);
    }

    @Test
    @Transactional
    @DisplayName("롤백 시: AFTER_COMMIT 리스너는 호출되지 않고, Notification도 저장되지 않음")
    void notifyFeedCommented_rollback_listenerNotInvoked_andNotificationNotPersisted() {
        // given
        Long actorUserId = 201L;
        String actorUsername = "bob";
        Long feedId = 1000L;

        // when (트랜잭션 안)
        orchestrator.notifyFeedCommented(targetUserId, actorUserId, actorUsername, feedId);

        // 롤백 트리거
        TestTransaction.flagForRollback();
        TestTransaction.end(); // 커밋이 아니므로 AFTER_COMMIT 리스너는 실행되지 않음

        // then
        verify(feedNotificationDispatchUseCase, times(0)).handleFeedCommented(any());
        assertThat(notificationJpaRepository.findAll()).isEmpty();
    }
}
