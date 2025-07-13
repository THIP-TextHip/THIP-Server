package konkuk.thip.room.application.port.in.dto;

import lombok.Builder;

@Builder
public record RoomGetHomeJoinedListQuery(
        Long userId,
        int page
) {
}
