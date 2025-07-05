package konkuk.thip.vote.adapter.in.web.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import konkuk.thip.vote.application.port.in.dto.VoteCreateCommand;

import java.util.List;

public record VoteCreateRequest(
        @NotNull(message = "page는 필수입니다.")
        Integer page,

        @NotNull(message = "isOverview(= 총평 여부)는 필수입니다.")
        Boolean isOverview,

        @NotBlank(message = "투표 내용은 필수입니다.")
        @Size(max = 20, message = "투표 내용은 최대 20자 입니다.")
        String content,

        @NotNull(message = "투표 항목은 필수입니다.")
        @Size(min = 1, max = 5, message = "투표 항목은 1개 이상, 최대 5개까지입니다.")
        @Valid
        List<VoteItemCreateRequest> voteItemList
) {
        public record VoteItemCreateRequest(
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
