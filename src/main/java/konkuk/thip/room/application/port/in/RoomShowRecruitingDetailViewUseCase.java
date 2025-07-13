package konkuk.thip.room.application.port.in;

import konkuk.thip.room.adapter.in.web.response.RoomRecruitingDetailViewResponse;

public interface RoomShowRecruitingDetailViewUseCase {

    RoomRecruitingDetailViewResponse getRecruitingRoomDetailView(Long userId, Long roomId);
}
