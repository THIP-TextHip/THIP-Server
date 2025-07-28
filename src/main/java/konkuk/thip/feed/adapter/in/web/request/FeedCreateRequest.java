package konkuk.thip.feed.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import konkuk.thip.feed.application.port.in.dto.FeedCreateCommand;

import java.util.List;

@Schema(description = "피드 생성 요청 DTO")
public record FeedCreateRequest(

        @Schema(description = "생성할 피드의 책 ISBN", example = "9788936433862")
        @NotBlank(message = "ISBN은 필수입니다.")
        String isbn,

        @Schema(description = "피드 내용", example = "이 책은 정말 좋습니다!")
        @NotBlank(message = "콘텐츠 내용은 필수입니다.")
        String contentBody,

        @Schema(description = "방 공개 설정 여부 (true: 공개, false: 비공개)", example = "true")
        @NotNull(message = "방 공개 설정 여부는 필수입니다.")
        Boolean isPublic,

        @Schema(description = "피드에 추가할 태그들", example = "[\"한국소설\", \"외국소설\", \"시\"]")
        List<String> tagList
) {
        public FeedCreateCommand toCommand(Long userId) {
                return new FeedCreateCommand(
                        isbn,
                        contentBody,
                        isPublic,
                        tagList,
                        userId
                );
        }
}
