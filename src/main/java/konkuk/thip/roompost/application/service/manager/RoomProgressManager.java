package konkuk.thip.roompost.application.service.manager;

import konkuk.thip.book.application.port.out.BookCommandPort;
import konkuk.thip.book.domain.Book;
import konkuk.thip.common.annotation.HelperService;
import konkuk.thip.room.application.port.out.RoomCommandPort;
import konkuk.thip.room.application.port.out.RoomParticipantCommandPort;
import konkuk.thip.room.domain.Room;
import konkuk.thip.room.domain.RoomParticipant;
import lombok.RequiredArgsConstructor;

import java.util.List;

@HelperService
@RequiredArgsConstructor
public class RoomProgressManager {

    private final RoomParticipantCommandPort roomParticipantCommandPort;
    private final RoomCommandPort roomCommandPort;
    private final BookCommandPort bookCommandPort;

    public void updateUserAndRoomProgress(Long userId, Long roomId, int currentPage) {
        RoomParticipant roomParticipant = roomParticipantCommandPort.getByUserIdAndRoomIdOrThrow(userId, roomId);
        Room room = roomCommandPort.getByIdOrThrow(roomId);
        Book book = bookCommandPort.findById(room.getBookId());

        // 1. 유저 진행률 update
        boolean updated = roomParticipant.updateUserProgress(currentPage, book.getPageCount());
        if (!updated) return;     // update 되지 않았으면 종료

        // 2. 방 평균 진행률 update
        List<RoomParticipant> all = roomParticipantCommandPort.findAllByRoomId(roomId);
        double total = all.stream()
                .filter(p -> !roomParticipant.getId().equals(p.getId()))    // 현재 유저 제외
                .mapToDouble(RoomParticipant::getUserPercentage)
                .sum();
        total += roomParticipant.getUserPercentage();
        room.updateRoomPercentage(total / all.size());

        // 3. 영속화
        roomCommandPort.update(room);
        roomParticipantCommandPort.update(roomParticipant);
    }

    public void removeUserProgressAndUpdateRoomProgress(Long removeRoomParticipantId, Long roomId) {

        Room room = roomCommandPort.getByIdOrThrow(roomId);

        // 나간 유저를 제외한 방 평균 진행률 update
        List<RoomParticipant> remainingParticipants = roomParticipantCommandPort.findAllByRoomId(roomId);
        double total = remainingParticipants.stream()
                .filter(p -> !p.getId().equals(removeRoomParticipantId)) // 나간 유저 제외
                .mapToDouble(RoomParticipant::getUserPercentage)
                .sum();
        room.updateRoomPercentage(total / (remainingParticipants.size() - 1));

        // 방 멤버 수 감소
        room.decreaseMemberCount();
        // 영속화
        roomCommandPort.update(room);
    }

}
