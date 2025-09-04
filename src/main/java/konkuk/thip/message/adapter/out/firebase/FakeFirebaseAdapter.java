package konkuk.thip.message.adapter.out.firebase;

import com.google.firebase.messaging.Message;
import konkuk.thip.message.application.port.out.FirebaseMessagingPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Profile("local | test")
@RequiredArgsConstructor
public class FakeFirebaseAdapter implements FirebaseMessagingPort {
    @Override
    public void send(Message message, String fcmToken, String deviceId) {
        log.info("FirebaseAdapter.send()");
    }

    @Override
    public void sendBatch(List<Message> messages, List<String> fcmTokens, List<String> deviceIds) {
        log.info("FirebaseAdapter.sendBatch()");
    }
}
