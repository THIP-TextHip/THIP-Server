package konkuk.thip.feed.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import konkuk.thip.feed.application.port.in.dto.FeedUpdateCommand;

import java.util.List;

public record FeedUpdateRequest(

        @Schema(description = "수정한 피드 내용", example = "이 책은 정말 좋습니다!")
        String contentBody,

        @Schema(description = "수정한 방 공개 설정 여부 (true: 공개, false: 비공개)", example = "true")
        Boolean isPublic,

        @Schema(description = "수정된 피드에 남아있는 태그들", example = "[\"한국소설\", \"외국소설\", \"시\"]")
        List<String> tagList,

        @Schema(description = "수정된 피드에 남아있는 이미지 URL들", example = "[\"https://img.domain.com/1.jpg\", \"https://img.domain.com/2.jpg\"]")
        List<String> remainImageUrls
) {
        public FeedUpdateCommand toCommand(Long userId, Long feedId) {
                return new FeedUpdateCommand(
                        contentBody,
                        isPublic,
                        tagList,
                        remainImageUrls,
                        userId,
                        feedId
                );
        }
}