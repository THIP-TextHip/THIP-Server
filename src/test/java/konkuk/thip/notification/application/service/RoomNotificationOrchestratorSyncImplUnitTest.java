package konkuk.thip.notification.application.service;

import konkuk.thip.message.application.port.out.RoomEventCommandPort;
import konkuk.thip.notification.application.port.out.NotificationCommandPort;
import konkuk.thip.notification.domain.Notification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("[단위] 모임방 알림 (동기화 방식) 헬퍼 서비스 단위 테스트")
class RoomNotificationOrchestratorSyncImplUnitTest {

    @Mock NotificationCommandPort notificationCommandPort;
    @Mock RoomEventCommandPort roomEventCommandPort;

    @InjectMocks RoomNotificationOrchestratorSyncImpl sut;

    @Test
    @DisplayName("모임방 게시글에 댓글: DB 저장 + 이벤트 퍼블리시")
    void notify_room_post_commented() {
        // given
        Long targetUserId = 10L;
        Long actorUserId = 20L;
        String actorUsername = "alice";
        Long roomId = 1L; int page = 2; Long postId = 3L; String postType = "RECORD";

        // when
        sut.notifyRoomPostCommented(targetUserId, actorUserId, actorUsername, roomId, page, postId, postType);

        // then 1) DB 저장값
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationCommandPort).save(captor.capture());
        Notification saved = captor.getValue();
        assertThat(saved.getTargetUserId()).isEqualTo(targetUserId);
        assertThat(saved.getTitle()).isNotBlank();
        assertThat(saved.getContent()).contains(actorUsername);

        // then 2) 이벤트 퍼블리시
        verify(roomEventCommandPort).publishRoomPostCommentedEvent(
                targetUserId, actorUserId, actorUsername, roomId, page, postId, postType
        );
    }

    @Test
    @DisplayName("모임방 투표 시작: DB 저장 + 이벤트 퍼블리시")
    void notify_room_vote_started() {
        // given
        Long targetUserId = 11L;
        Long roomId = 101L; String roomTitle = "독서방"; int page = 1; Long postId = 999L;

        // when
        sut.notifyRoomVoteStarted(targetUserId, roomId, roomTitle, page, postId);

        // then
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationCommandPort).save(captor.capture());
        Notification saved = captor.getValue();
        assertThat(saved.getTargetUserId()).isEqualTo(targetUserId);
        assertThat(saved.getTitle()).contains(roomTitle);
        assertThat(saved.getContent()).isNotBlank();

        verify(roomEventCommandPort).publishRoomVoteStartedEvent(
                targetUserId, roomId, roomTitle, page, postId
        );
    }

    @Test
    @DisplayName("모임방 기록 작성: DB 저장 + 이벤트 퍼블리시")
    void notify_room_record_created() {
        // given
        Long targetUserId = 12L;
        Long actorUserId = 22L;
        String actorUsername = "bob";
        Long roomId = 201L; String roomTitle = "역사방"; int page = 3; Long postId = 1001L;

        // when
        sut.notifyRoomRecordCreated(targetUserId, actorUserId, actorUsername, roomId, roomTitle, page, postId);

        // then
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationCommandPort).save(captor.capture());
        Notification saved = captor.getValue();
        assertThat(saved.getTargetUserId()).isEqualTo(targetUserId);
        assertThat(saved.getTitle()).isNotBlank();
        assertThat(saved.getContent()).contains(actorUsername);

        verify(roomEventCommandPort).publishRoomRecordCreatedEvent(
                targetUserId, actorUserId, actorUsername, roomId, roomTitle, page, postId
        );
    }

    @Test
    @DisplayName("모집 조기 마감: DB 저장 + 이벤트 퍼블리시")
    void notify_room_recruit_closed_early() {
        // given
        Long targetUserId = 13L;
        Long roomId = 301L; String roomTitle = "과학방";

        // when
        sut.notifyRoomRecruitClosedEarly(targetUserId, roomId, roomTitle);

        // then
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationCommandPort).save(captor.capture());
        Notification saved = captor.getValue();
        assertThat(saved.getTargetUserId()).isEqualTo(targetUserId);
        assertThat(saved.getTitle()).contains(roomTitle);
        assertThat(saved.getContent()).isNotBlank();

        verify(roomEventCommandPort).publishRoomRecruitClosedEarlyEvent(
                targetUserId, roomId, roomTitle
        );
    }

    @Test
    @DisplayName("모임 활동 시작: DB 저장 + 이벤트 퍼블리시")
    void notify_room_activity_started() {
        // given
        Long targetUserId = 14L;
        Long roomId = 401L; String roomTitle = "문학방";

        // when
        sut.notifyRoomActivityStarted(targetUserId, roomId, roomTitle);

        // then
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationCommandPort).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getTargetUserId()).isEqualTo(targetUserId);
        assertThat(saved.getTitle()).contains(roomTitle);
        assertThat(saved.getContent()).isNotBlank();

        verify(roomEventCommandPort).publishRoomActivityStartedEvent(
                targetUserId, roomId, roomTitle
        );
    }

    @Test
    @DisplayName("호스트에게 참여 알림: DB 저장 + 이벤트 퍼블리시")
    void notify_room_join_to_host() {
        // given
        Long hostUserId = 15L;
        Long actorUserId = 25L;
        String actorUsername = "carol";
        Long roomId = 501L; String roomTitle = "미술방";

        // when
        sut.notifyRoomJoinToHost(hostUserId, roomId, roomTitle, actorUserId, actorUsername);

        // then
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationCommandPort).save(captor.capture());
        Notification saved = captor.getValue();
        assertThat(saved.getTargetUserId()).isEqualTo(hostUserId);
        assertThat(saved.getTitle()).isNotBlank();
        assertThat(saved.getContent()).contains(actorUsername);

        verify(roomEventCommandPort).publishRoomJoinEventToHost(
                hostUserId, roomId, roomTitle, actorUserId, actorUsername
        );
    }

    @Test
    @DisplayName("모임 댓글 좋아요: DB 저장 + 이벤트 퍼블리시")
    void notify_room_comment_liked() {
        // given
        Long targetUserId = 16L;
        Long actorUserId = 26L;
        String actorUsername = "dave";
        Long roomId = 601L; int page = 9; Long postId = 777L; String postType = "RECORD";

        // when
        sut.notifyRoomCommentLiked(targetUserId, actorUserId, actorUsername, roomId, page, postId, postType);

        // then
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationCommandPort).save(captor.capture());
        Notification saved = captor.getValue();
        assertThat(saved.getTargetUserId()).isEqualTo(targetUserId);
        assertThat(saved.getTitle()).isNotBlank();
        assertThat(saved.getContent()).contains(actorUsername);

        verify(roomEventCommandPort).publishRoomCommentLikedEvent(
                targetUserId, actorUserId, actorUsername, roomId, page, postId, postType
        );
    }

    @Test
    @DisplayName("모임 게시글 좋아요: DB 저장 + 이벤트 퍼블리시")
    void notify_room_post_liked() {
        // given
        Long targetUserId = 17L;
        Long actorUserId = 27L;
        String actorUsername = "erin";
        Long roomId = 701L; int page = 5; Long postId = 888L; String postType = "RECORD";

        // when
        sut.notifyRoomPostLiked(targetUserId, actorUserId, actorUsername, roomId, page, postId, postType);

        // then
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationCommandPort).save(captor.capture());
        Notification saved = captor.getValue();
        assertThat(saved.getTargetUserId()).isEqualTo(targetUserId);
        assertThat(saved.getTitle()).isNotBlank();
        assertThat(saved.getContent()).contains(actorUsername);

        verify(roomEventCommandPort).publishRoomPostLikedEvent(
                targetUserId, actorUserId, actorUsername, roomId, page, postId, postType
        );
    }

    @Test
    @DisplayName("모임 대댓글: DB 저장 + 이벤트 퍼블리시")
    void notify_room_post_comment_replied() {
        // given
        Long targetUserId = 18L;
        Long actorUserId = 28L;
        String actorUsername = "frank";
        Long roomId = 801L; int page = 6; Long postId = 999L; String postType = "RECORD";

        // when
        sut.notifyRoomPostCommentReplied(targetUserId, actorUserId, actorUsername, roomId, page, postId, postType);

        // then
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationCommandPort).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getTargetUserId()).isEqualTo(targetUserId);
        assertThat(saved.getTitle()).isNotBlank();
        assertThat(saved.getContent()).contains(actorUsername);

        verify(roomEventCommandPort).publishRoomPostCommentRepliedEvent(
                targetUserId, actorUserId, actorUsername, roomId, page, postId, postType
        );
    }
}
