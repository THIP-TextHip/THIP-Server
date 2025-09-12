package konkuk.thip.notification.application.port.out;


import konkuk.thip.notification.domain.Notification;

public interface NotificationCommandPort {

    void save(Notification notification);
}
