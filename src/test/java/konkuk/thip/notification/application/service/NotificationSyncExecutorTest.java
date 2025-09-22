package konkuk.thip.notification.application.service;

import konkuk.thip.notification.application.port.out.NotificationCommandPort;
import konkuk.thip.notification.application.service.template.NotificationTemplate;
import konkuk.thip.notification.domain.Notification;
import konkuk.thip.notification.domain.value.NotificationCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

@DisplayName("[단위] NotificationSyncExecutor - 동기 알림 저장 및 이벤트 퍼블리시")
class NotificationSyncExecutorTest {

    @Test
    @DisplayName("execute() 메서드 : 푸시 알림 이벤트 퍼블리시 과정에서 예외가 발생하더라도 예외를 외부로 던지지 않는다.")
    void execute_publish_failure_does_not_throw() {
        // given
        NotificationCommandPort commandPort = mock(NotificationCommandPort.class);
        when(commandPort.save(any(Notification.class))).thenReturn(42L);    // save 시 생성된 notificationId 를 리턴하도록 스텁

        NotificationSyncExecutor executor = new NotificationSyncExecutor(commandPort);

        // 간단한 템플릿 스텁 (title/content 고정)
        NotificationTemplate<String> template = new NotificationTemplate<>() {
            @Override
            public String title(String args) { return "테스트제목"; }
            @Override
            public String content(String args) { return "테스트내용"; }
            @Override
            public NotificationCategory notificationCategory(String args) { return NotificationCategory.FEED; }
        };

        // publish 호출 시 강제로 예외를 던지는 invoker
        EventCommandInvoker invoker = (title, content, notificationId) -> {
            throw new RuntimeException("강제 퍼블리시 실패");
        };

        // when & then
        assertThatCode(() ->
                executor.execute(template, "dummyArgs", 123L, invoker)
        ).doesNotThrowAnyException();

        // NotificationCommandPort은 정상적으로 호출되었는지 검증
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(commandPort, times(1)).save(captor.capture());

        Notification saved = captor.getValue();
        // 템플릿에서 설정한 title/content 값이 그대로 들어갔는지 확인
        assertThat(saved.getTitle()).isEqualTo("테스트제목");
        assertThat(saved.getContent()).isEqualTo("테스트내용");
        assertThat(saved.getTargetUserId()).isEqualTo(123L);
        assertThat(saved.getNotificationCategory()).isEqualTo(NotificationCategory.FEED);
    }
}
