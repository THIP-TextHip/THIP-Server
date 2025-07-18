package konkuk.thip.feed.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import konkuk.thip.feed.application.port.in.dto.FeedCreateCommand;

import java.util.List;

public record FeedCreateRequest(

        @NotBlank(message = "ISBN은 필수입니다.")
        String isbn,

        @NotBlank(message = "콘텐츠 내용은 필수입니다.")
        String contentBody,

        @NotNull(message = "방 공개 설정 여부는 필수입니다.")
        Boolean isPublic,

        String category,

        List<String> tagList
) {
        public FeedCreateCommand toCommand(Long userId) {
                return new FeedCreateCommand(
                        isbn,
                        contentBody,
                        isPublic,
                        category,
                        tagList,
                        userId
                );
        }
}
