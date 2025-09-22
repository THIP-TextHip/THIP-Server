package konkuk.thip.notification.application.service;

@FunctionalInterface
public interface EventCommandInvoker {

    void publish(String title, String content, Long notificationId);
}
