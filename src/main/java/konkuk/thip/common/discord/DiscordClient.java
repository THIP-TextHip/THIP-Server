package konkuk.thip.common.discord;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordClient {

    private final WebClient webClient;

    @Value("${discord.env}")
    private String env;

    @Value("${discord.webhook-url}")
    private String webhookUrl;

    public void sendErrorMessage(String message, String stackTrace, String requestId, String userId) {
        if ("test".equals(env)) return;

        Map<String, Object> embedData = new HashMap<>();
        embedData.put("title", "THIP 서버 500 에러 발생");

        Map<String, String> field1 = new HashMap<>();
        field1.put("name", "발생시각");
        field1.put("value", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        Map<String, String> field2 = new HashMap<>();
        field2.put("name", "에러 명");
        field2.put("value", message);

        Map<String, String> field3 = new HashMap<>();
        field3.put("name", "스택 트레이스");
        field3.put("value", stackTrace);

        Map<String, String> field4 = new HashMap<>();
        field4.put("name", "Request ID");
        field4.put("value", requestId != null ? requestId : "N/A");

        Map<String, String> field5 = new HashMap<>();
        field5.put("name", "User ID");
        field5.put("value", userId != null ? userId : "N/A");

        embedData.put("fields", List.of(field1, field2, field3, field4, field5));

        Map<String, Object> payload = new HashMap<>();
        payload.put("embeds", new Object[]{embedData});

        sendSync(payload, "서버 500 에러");
    }

    public void sendAladinApiFailureAlert(String isbn, Exception e) {
        if ("test".equals(env)) return;

        Map<String, Object> embedData = new HashMap<>();
        embedData.put("title", "알라딘 API 페이지 정보 획득 실패");

        Map<String, String> field1 = new HashMap<>();
        field1.put("name", "발생시각");
        field1.put("value", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        Map<String, String> field2 = new HashMap<>();
        field2.put("name", "ISBN");
        field2.put("value", isbn);

        Map<String, String> field3 = new HashMap<>();
        field3.put("name", "에러 메시지");
        field3.put("value", e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());

        embedData.put("fields", List.of(field1, field2, field3));

        Map<String, Object> payload = new HashMap<>();
        payload.put("embeds", new Object[]{embedData});

        sendAsync(payload, "알라딘 API 실패 알림 (isbn: " + isbn + ")");
    }

    public void sendPageCountFillResult(int total, int successCount, int unfindableCount, int transientFailCount) {
        if ("test".equals(env)) return;

        Map<String, Object> embedData = new HashMap<>();
        embedData.put("title", "[스케줄러] pageCount 업데이트 실행 완료");

        Map<String, String> field1 = new HashMap<>();
        field1.put("name", "처리 대상");
        field1.put("value", total + "건");

        Map<String, String> field2 = new HashMap<>();
        field2.put("name", "pageCount 업데이트 성공");
        field2.put("value", successCount + "건");

        Map<String, String> field3 = new HashMap<>();
        field3.put("name", "알라딘 미등록 (unfindable 처리)");
        field3.put("value", unfindableCount + "건");

        Map<String, String> field4 = new HashMap<>();
        field4.put("name", "일시 실패 (다음 실행 재시도)");
        field4.put("value", transientFailCount + "건");

        embedData.put("fields", List.of(field1, field2, field3, field4));

        Map<String, Object> payload = new HashMap<>();
        payload.put("embeds", new Object[]{embedData});

        sendSync(payload, "pageCount 스케줄러 결과");
    }

    private void sendSync(Map<String, Object> payload, String alertDescription) {
        try {
            webClient.post()
                    .uri(webhookUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (Exception ex) {
            log.warn("[Discord] 알림 전송 실패 - {}: {}", alertDescription, ex.getMessage());
        }
    }

    private void sendAsync(Map<String, Object> payload, String alertDescription) {
        webClient.post()
                .uri(webhookUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Void.class)
                .subscribe(
                        null,
                        ex -> log.warn("[Discord] 알림 전송 실패 - {}: {}", alertDescription, ex.getMessage())
                );
    }
}
