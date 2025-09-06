package konkuk.thip.message.adapter.out.firebase;

import com.google.firebase.messaging.*;
import konkuk.thip.common.exception.FirebaseException;
import konkuk.thip.message.application.port.out.FirebaseMessagingPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Profile("prod | dev")
@RequiredArgsConstructor
public class FirebaseAdapter implements FirebaseMessagingPort {

    private final FirebaseMessaging firebaseMessaging;

    @Override
    public void send(Message message, String fcmToken, String deviceId) {
        try {
            firebaseMessaging.send(message);
        } catch (FirebaseMessagingException e) {
            throw new FirebaseException(e);
        }
    }

    @Override
    public void sendBatch(List<Message> messages, List<String> fcmTokens, List<String> deviceIds) {
        if (messages.size() != fcmTokens.size() || messages.size() != deviceIds.size()) {
            throw new FirebaseException(new IllegalArgumentException("메시지, FCM 토큰, 디바이스 ID 리스트의 크기는 같아야 합니다."));
        }

        try {
            BatchResponse batchResponse = firebaseMessaging.sendEach(messages);

            if (batchResponse.getFailureCount() > 0) {
                log.warn("[FCM:BATCH] 일부 메시지 전송 실패: {}/{}", batchResponse.getFailureCount(), messages.size());
                throw new FirebaseException();
            }
        } catch (FirebaseMessagingException e) {
            log.warn("[FCM:BATCH] 메시지 전송 실패: {}", e.getMessage());
            throw new FirebaseException(e);
        }
    }
}
