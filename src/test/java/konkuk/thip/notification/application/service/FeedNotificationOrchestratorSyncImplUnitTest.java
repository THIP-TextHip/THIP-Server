package konkuk.thip.notification.application.service;

import konkuk.thip.message.application.port.out.FeedEventCommandPort;
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
@DisplayName("[단위] 피드 알림 (동기화 방식) 헬퍼 서비스 단위 테스트")
class FeedNotificationOrchestratorSyncImplUnitTest {

    @Mock NotificationCommandPort notificationCommandPort;
    @Mock FeedEventCommandPort feedEventCommandPort;

    @InjectMocks FeedNotificationOrchestratorSyncImpl sut;

    @Test
    @DisplayName("피드 댓글 알림: DB 저장 + 이벤트 퍼블리시")
    void notify_feed_commented_test() {
        // given
        Long targetUserId = 10L;
        Long actorUserId = 20L;
        String actorUsername = "alice";
        Long feedId = 99L;

        // when
        sut.notifyFeedCommented(targetUserId, actorUserId, actorUsername, feedId);

        // then 1) DB 저장 포트 호출 값 검증
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationCommandPort).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getTargetUserId()).isEqualTo(targetUserId);
        assertThat(saved.getTitle()).isNotBlank();
        assertThat(saved.getContent()).contains(actorUsername);

        // then 2) 이벤트 퍼블리시 포트 호출 검증
        verify(feedEventCommandPort)
                .publishFeedCommentedEvent(targetUserId, actorUserId, actorUsername, feedId);
    }

    @Test
    @DisplayName("팔로우 알림: DB 저장 + 이벤트 퍼블리시")
    void notify_followed_test() {
        // given
        Long targetUserId = 11L;
        Long actorUserId = 21L;
        String actorUsername = "bob";

        // when
        sut.notifyFollowed(targetUserId, actorUserId, actorUsername);

        // then: DB 저장값 검증
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationCommandPort).save(captor.capture());
        Notification saved = captor.getValue();
        assertThat(saved.getTargetUserId()).isEqualTo(targetUserId);
        assertThat(saved.getTitle()).isNotBlank();
        assertThat(saved.getContent()).contains(actorUsername);

        // then: 이벤트 퍼블리시 검증
        verify(feedEventCommandPort)
                .publishFollowEvent(targetUserId, actorUserId, actorUsername);
    }

    @Test
    @DisplayName("피드 대댓글 알림: DB 저장 + 이벤트 퍼블리시")
    void notify_feed_replied_test() {
        // given
        Long targetUserId = 12L;
        Long actorUserId = 22L;
        String actorUsername = "carol";
        Long feedId = 100L;

        // when
        sut.notifyFeedReplied(targetUserId, actorUserId, actorUsername, feedId);

        // then: DB 저장값 검증
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationCommandPort).save(captor.capture());
        Notification saved = captor.getValue();
        assertThat(saved.getTargetUserId()).isEqualTo(targetUserId);
        assertThat(saved.getTitle()).isNotBlank();
        assertThat(saved.getContent()).contains(actorUsername);

        // then: 이벤트 퍼블리시 검증
        verify(feedEventCommandPort)
                .publishFeedRepliedEvent(targetUserId, actorUserId, actorUsername, feedId);
    }

    @Test
    @DisplayName("팔로우한 사람의 새 글 알림: DB 저장 + 이벤트 퍼블리시")
    void notify_followee_new_post_test() {
        // given
        Long targetUserId = 13L;
        Long actorUserId = 23L;
        String actorUsername = "dave";
        Long feedId = 101L;

        // when
        sut.notifyFolloweeNewPost(targetUserId, actorUserId, actorUsername, feedId);

        // then: DB 저장값 검증
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationCommandPort).save(captor.capture());
        Notification saved = captor.getValue();
        assertThat(saved.getTargetUserId()).isEqualTo(targetUserId);
        assertThat(saved.getTitle()).isNotBlank();
        assertThat(saved.getContent()).contains(actorUsername);

        // then: 이벤트 퍼블리시 검증
        verify(feedEventCommandPort)
                .publishFolloweeNewPostEvent(targetUserId, actorUserId, actorUsername, feedId);
    }

    @Test
    @DisplayName("피드 좋아요 알림: DB 저장 + 이벤트 퍼블리시")
    void notify_feed_liked_test() {
        // given
        Long targetUserId = 14L;
        Long actorUserId = 24L;
        String actorUsername = "eve";
        Long feedId = 102L;

        // when
        sut.notifyFeedLiked(targetUserId, actorUserId, actorUsername, feedId);

        // then: DB 저장값 검증
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationCommandPort).save(captor.capture());
        Notification saved = captor.getValue();
        assertThat(saved.getTargetUserId()).isEqualTo(targetUserId);
        assertThat(saved.getTitle()).isNotBlank();
        assertThat(saved.getContent()).contains(actorUsername);

        // then: 이벤트 퍼블리시 검증
        verify(feedEventCommandPort)
                .publishFeedLikedEvent(targetUserId, actorUserId, actorUsername, feedId);
    }

    @Test
    @DisplayName("피드 댓글 좋아요 알림: DB 저장 + 이벤트 퍼블리시")
    void notify_feed_comment_liked_test() {
        // given
        Long targetUserId = 15L;
        Long actorUserId = 25L;
        String actorUsername = "frank";
        Long feedId = 103L;

        // when
        sut.notifyFeedCommentLiked(targetUserId, actorUserId, actorUsername, feedId);

        // then: DB 저장값 검증
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationCommandPort).save(captor.capture());
        Notification saved = captor.getValue();
        assertThat(saved.getTargetUserId()).isEqualTo(targetUserId);
        assertThat(saved.getTitle()).isNotBlank();
        assertThat(saved.getContent()).contains(actorUsername);

        // then: 이벤트 퍼블리시 검증
        verify(feedEventCommandPort)
                .publishFeedCommentLikedEvent(targetUserId, actorUserId, actorUsername, feedId);
    }
}
