package konkuk.thip.notification.application.service;

import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.message.adapter.out.event.dto.RoomEvents;
import konkuk.thip.message.application.port.in.RoomNotificationDispatchUseCase;
import konkuk.thip.notification.adapter.out.jpa.NotificationJpaEntity;
import konkuk.thip.notification.adapter.out.persistence.repository.NotificationJpaRepository;
import konkuk.thip.notification.application.port.in.RoomNotificationOrchestrator;
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
@DisplayName("[통합] 모임방 알림 (동기화 방식) 헬퍼 서비스 통합 테스트")
class RoomNotificationOrchestratorSyncImplTest {

    @Autowired RoomNotificationOrchestrator orchestrator; // 반드시 인터페이스 타입으로 주입(트랜잭션 프록시 적용)
    @Autowired NotificationJpaRepository notificationJpaRepository;
    @Autowired UserJpaRepository userJpaRepository;

    @MockitoBean RoomNotificationDispatchUseCase roomNotificationDispatchUseCase;

    private Long targetUserId;

    @BeforeEach
    void setUp() {
        // Notification FK를 만족시키기 위한 대상 사용자 준비
        UserJpaEntity target = userJpaRepository.save(TestEntityFactory.createUser(Alias.WRITER, "노성준"));
        targetUserId = target.getUserId();
    }

    @AfterEach
    void tearDown() {
        notificationJpaRepository.deleteAllInBatch();
        userJpaRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("상위 트랜잭션 없이 호출하면 IllegalTransactionStateException 발생 (MANDATORY)")
    void mandatory_without_transaction_throws() {
        // given
        Long actorUserId = 200L;
        String actorUsername = "carol";
        Long roomId = 11L;
        Integer page = 1;
        Long postId = 22L;
        String postType = "RECORD";

        // when & then
        assertThatThrownBy(() ->
                orchestrator.notifyRoomPostCommented(
                        targetUserId, actorUserId, actorUsername, roomId, page, postId, postType
                )
        ).isInstanceOf(IllegalTransactionStateException.class);
    }

    @Test
    @Transactional  // 상위 트랜잭션 존재
    @DisplayName("상위 트랜잭션 안에서 호출하면 정상 동작하고, Notification이 저장된다")
    void mandatory_with_transaction_succeeds_and_persists() {
        // given
        Long actorUserId = 201L;
        String actorUsername = "dave";
        Long roomId = 12L;
        Integer page = 3;
        Long postId = 33L;
        String postType = "RECORD";

        // when
        orchestrator.notifyRoomPostCommented(
                targetUserId, actorUserId, actorUsername, roomId, page, postId, postType
        );

        // then
        var all = notificationJpaRepository.findAll();
        assertThat(all).hasSize(1);

        NotificationJpaEntity saved = all.get(0);
        assertThat(saved.getUserJpaEntity().getUserId()).isEqualTo(targetUserId);
        assertThat(saved.getTitle()).isNotBlank();
        assertThat(saved.getContent()).contains(actorUsername);
    }

    @Test
    @Transactional
    @DisplayName("커밋 시: AFTER_COMMIT 리스너가 handleRoomPostCommented 호출 & Notification 커밋됨")
    void roomPostCommented_afterCommit_listenerInvoked_andNotificationCommitted() {
        // given
        Long actorUserId = 301L;
        String actorUsername = "alice";
        Long roomId = 1001L;
        Integer page = 7;
        Long postId = 5001L;
        String postType = "RECORD";

        // when (트랜잭션 안)
        orchestrator.notifyRoomPostCommented(
                targetUserId, actorUserId, actorUsername, roomId, page, postId, postType
        );

        // 실제 커밋 트리거 → AFTER_COMMIT 리스너 실행 (test 프로필은 @Async 동기화)
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // then : 리스너에 전달되는 DTO 필드 검증
        ArgumentCaptor<RoomEvents.RoomPostCommentedEvent> captor =
                ArgumentCaptor.forClass(RoomEvents.RoomPostCommentedEvent.class);
        verify(roomNotificationDispatchUseCase).handleRoomPostCommented(captor.capture());

        NotificationJpaEntity saved = notificationJpaRepository.findAll().get(0);

        RoomEvents.RoomPostCommentedEvent event = captor.getValue();
        assertThat(event).isNotNull();
        assertThat(event.title()).isNotBlank();
        assertThat(event.content()).contains(actorUsername);
        assertThat(event.targetUserId()).isEqualTo(targetUserId);
        assertThat(event.notificationId()).isEqualTo(saved.getNotificationId());
    }

    @Test
    @Transactional
    @DisplayName("롤백 시: AFTER_COMMIT 리스너는 호출되지 않고, Notification도 저장되지 않음")
    void roomPostCommented_rollback_listenerNotInvoked_andNotificationNotCommitted() {
        // given
        Long actorUserId = 302L;
        String actorUsername = "bob";
        Long roomId = 1002L;
        Integer page = 2;
        Long postId = 5002L;
        String postType = "RECORD";

        // when
        orchestrator.notifyRoomPostCommented(
                targetUserId, actorUserId, actorUsername, roomId, page, postId, postType
        );

        // 롤백 트리거 → AFTER_COMMIT 미실행
        TestTransaction.flagForRollback();
        TestTransaction.end();

        // then
        verify(roomNotificationDispatchUseCase, times(0)).handleRoomPostCommented(any());
        assertThat(notificationJpaRepository.findAll()).isEmpty();
    }
}
