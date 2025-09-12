package konkuk.thip.notification.application.service;

import konkuk.thip.common.util.TestEntityFactory;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("[통합] 모임방 알림 (동기화 방식) 헬퍼 서비스 통합 테스트")
class RoomNotificationOrchestratorSyncImplTest {

    @Autowired RoomNotificationOrchestrator orchestrator; // 반드시 인터페이스 타입으로 주입(트랜잭션 프록시 적용)
    @Autowired NotificationJpaRepository notificationJpaRepository;
    @Autowired UserJpaRepository userJpaRepository;

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
}
