package konkuk.thip.user.application.port.in;

public interface UserShowNotificationSettingsInfoUseCase {
    boolean getUserNotificationSettingsInfo(Long userId,String deviceId);
}
