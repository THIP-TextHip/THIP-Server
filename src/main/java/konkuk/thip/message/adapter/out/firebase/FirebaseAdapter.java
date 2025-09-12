package konkuk.thip.message.adapter.out.firebase;

import com.google.firebase.messaging.*;
import konkuk.thip.common.exception.FirebaseException;
import konkuk.thip.message.application.port.out.FirebaseMessagingPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Profile("!test & !local")
@RequiredArgsConstructor
public class FirebaseAdapter implements FirebaseMessagingPort {

    private final FirebaseMessaging firebaseMessaging;

    @Value("${server.profile}")
    private String profile;

    @Override
    public void send(Message message, String fcmToken, String deviceId) {
        try {
            String messageId = firebaseMessaging.send(message);
            log.debug("[FCM:SEND] ok id={} token={} device={}", messageId, maskDependingProfile(fcmToken), maskDependingProfile(deviceId));
        } catch (FirebaseMessagingException e) {
            log.warn("[FCM:SEND] fail token={} device={} code={} msg={}", maskDependingProfile(fcmToken), maskDependingProfile(deviceId), e.getMessagingErrorCode(), e.getMessage());
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

            List<SendResponse> responses = batchResponse.getResponses();
            for (int i = 0; i < responses.size(); i++) {
                SendResponse sr = responses.get(i);
                String token = fcmTokens.get(i);
                String device = deviceIds.get(i);

                if (sr.isSuccessful()) {
                    log.debug("[FCM:BATCH] ok id={} token={} device={}", sr.getMessageId(), maskDependingProfile(token), maskDependingProfile(device));
                } else {
                    Exception ex = sr.getException();
                    if (ex instanceof FirebaseMessagingException fme) {
                        log.warn("[FCM:BATCH] fail token={} device={} code={} msg={}", maskDependingProfile(token), maskDependingProfile(device), fme.getMessagingErrorCode(), fme.getMessage());
                    } else {
                        log.warn("[FCM:BATCH] fail token={} device={} msg={}", maskDependingProfile(token), maskDependingProfile(device), ex.getMessage());
                    }
                }
            }

            if (batchResponse.getFailureCount() > 0) {
                log.warn("[FCM:BATCH] 일부 메시지 전송 실패: {}/{}", batchResponse.getFailureCount(), messages.size());
                throw new FirebaseException();
            }
        } catch (FirebaseMessagingException e) {
            log.warn("[FCM:BATCH] 메시지 전송 실패: code={} msg={}", e.getMessagingErrorCode(), e.getMessage());
            throw new FirebaseException(e);
        }
    }

    /**
     * 프로파일별 마스킹 정책
     * - dev: 원문 그대로 노출
     * - prod: 전체 길이의 절반을 '*'로 치환(치환된 개수만큼 별표가 보이도록), 나머지는 앞/뒤를 균등하게 노출
     */
    private String maskDependingProfile(String value) {
        if (isDev()) return value;
        return maskHalf(value);
    }

    private boolean isDev() {
        return profile != null && profile.trim().equalsIgnoreCase("dev");
    }

    /**
     * 전체 길이의 절반을 '*'로 치환하고, 남은 절반은 앞/뒤를 균등 분할해 노출
     * 예) abcdefghij(10) -> ab*****hij (앞 2, 중간 5*, 뒤 3)
     */
    private String maskHalf(String s) {
        if (s == null || s.isEmpty()) return "null";
        int len = s.length();
        if (len <= 4) return "*".repeat(len); // 너무 짧으면 전부 마스킹

        int maskLen = len / 2;                 // 절반 마스킹
        int visible = len - maskLen;           // 보이는 길이
        int left = visible / 2;                // 앞쪽 보이는 길이
        int right = visible - left;            // 뒤쪽 보이는 길이

        String prefix = s.substring(0, left);
        String stars = "*".repeat(maskLen);
        String suffix = s.substring(len - right);
        return prefix + stars + suffix;
    }
}