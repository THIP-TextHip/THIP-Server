package konkuk.thip.notification.application.service.template;

public interface NotificationTemplate<T> {

    String title(T args);

    String content(T args);
}
