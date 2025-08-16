package konkuk.thip.room.adapter.in.web.response;

import lombok.Builder;

import java.util.List;

@Builder
public record RoomGetHomeJoinedListResponse(
        List<JoinedRoomInfo> roomList,
        String nickname,
        int page,       // 현재 페이지
        int size,       // 현재 페이지에 포함된 데이터 수
        boolean last,
        boolean first
) {


    @Builder
    public record JoinedRoomInfo(
            Long roomId,
            String bookImageUrl,
            String roomTitle,
            int memberCount,
            int userPercentage
    ) {}
}
