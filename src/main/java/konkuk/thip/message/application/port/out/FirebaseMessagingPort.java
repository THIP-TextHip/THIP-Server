package konkuk.thip.message.application.port.out;

import com.google.firebase.messaging.Message;

import java.util.List;

public interface FirebaseMessagingPort {
    void send(Message message, String fcmToken, String deviceId);

    void sendBatch(List<Message> messages, List<String> fcmTokens, List<String> deviceIds);
}
