package konkuk.thip.notification.application.port.in;

public interface NotificationShowEnableStateUseCase {
    boolean getNotificationShowEnableState(Long userId, String deviceId);
}
