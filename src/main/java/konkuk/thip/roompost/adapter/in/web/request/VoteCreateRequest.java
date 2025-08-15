package konkuk.thip.roompost.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import konkuk.thip.roompost.application.port.in.dto.vote.VoteCreateCommand;

import java.util.List;

@Schema(description = "투표 생성 요청 DTO")
public record VoteCreateRequest(
        @Schema(description = "투표를 생성할 책의 페이지 번호", example = "20")
        @NotNull(message = "page는 필수입니다.")
        Integer page,

        @Schema(description = "총평 여부", example = "true")
        @NotNull(message = "isOverview(= 총평 여부)는 필수입니다.")
        Boolean isOverview,

        @Schema(description = "투표 내용", example = "띱은 최고의 서비스인가?")
        @NotBlank(message = "투표 내용은 필수입니다.")
        @Size(max = 20, message = "투표 내용은 최대 20자 입니다.")
        String content,

        @Schema(description = "투표 항목 리스트", example = "[{\"itemName\": \"네\"}, {\"itemName\": \"아니오\"}]")
        @NotNull(message = "투표 항목은 필수입니다.")
        @Size(min = 1, max = 5, message = "투표 항목은 1개 이상, 최대 5개까지입니다.")
        @Valid
        List<VoteItemCreateRequest> voteItemList
) {
        @Schema(description = "투표 항목 DTO")
        public record VoteItemCreateRequest(

                @Schema(description = "투표 항목 이름", example = "네")
                @NotBlank(message = "투표 항목 이름은 필수입니다.")
                @Size(max = 20, message = "투표 항목 이름은 최대 20자입니다.")
                String itemName
        ) {}

        public VoteCreateCommand toCommand(Long userId, Long roomId) {
                List<VoteCreateCommand.VoteItemCreateCommand> mappedItems = voteItemList.stream()
                        .map(voteItem -> new VoteCreateCommand.VoteItemCreateCommand(voteItem.itemName))
                        .toList();

                return new VoteCreateCommand(
                        userId,
                        roomId,
                        page,
                        isOverview,
                        content,
                        mappedItems
                );
        }

}
