package konkuk.thip.user.application.port.out;

import konkuk.thip.user.domain.UserRoom;

import java.util.List;

public interface UserRoomCommandPort {

    UserRoom findByUserIdAndRoomId(Long userId, Long roomId);
    List<UserRoom> findAllByRoomId(Long roomId);

}
