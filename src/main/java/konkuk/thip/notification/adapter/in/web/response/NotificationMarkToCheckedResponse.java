package konkuk.thip.notification.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;
import konkuk.thip.notification.domain.value.MessageRoute;

import java.util.Map;

@Schema(description = "알림 읽음 처리 응답 DTO")
public record NotificationMarkToCheckedResponse(
        @Schema(description = "'알림 리다이렉트 목적지' 에 해당하는 enum 값 입니다.", example = "POST_DETAIL -> 게시글 상세 페이지로 이동한다는 의미")
        MessageRoute route,

        @Schema(description = "'알림 리다이렉트 목적지' 로 이동할 때 필요한 파라미터들 입니다.", example = "{\"postId\": 123}")
        Map<String, Object> params
) {
}
