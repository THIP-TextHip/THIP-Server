package konkuk.thip.room.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import konkuk.thip.post.application.port.in.dto.PostIsLikeCommand;
import konkuk.thip.room.domain.RoomPostType;

@Schema(description = "방기록 좋아요 상태 변경 요청 DTO")
public record RoomPostIsLikeRequest(
        @Schema(description = "좋아요 여부 type (true -> 좋아요, false -> 좋아요 취소)", example = "true")
        @NotNull(message = "좋아요 여부는 필수입니다.")
        Boolean type,

        @Schema(description = "게시물 타입 (RECORD, VOTE)", example = "RECORD")
        @NotBlank(message = "게시물 타입은 필수입니다.")
        String roomPostType
) {
    public PostIsLikeCommand toCommand(Long userId, Long postId) {
        return new PostIsLikeCommand(userId, postId, RoomPostType.from(roomPostType).toPostType(), type);
    }
}