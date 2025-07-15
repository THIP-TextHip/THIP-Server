package konkuk.thip.user.adapter.out.persistence;

import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.common.exception.code.ErrorCode;
import konkuk.thip.user.adapter.out.jpa.UserRoomJpaEntity;
import konkuk.thip.user.adapter.out.mapper.UserRoomMapper;
import konkuk.thip.user.adapter.out.persistence.repository.UserRoomJpaRepository;
import konkuk.thip.user.application.port.out.UserRoomCommandPort;
import konkuk.thip.user.domain.UserRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserRoomCommandPersistenceAdapter implements UserRoomCommandPort {

    private final UserRoomJpaRepository userRoomJpaRepository;
    private final UserRoomMapper userRoomMapper;

    @Override
    public UserRoom findByUserIdAndRoomId(Long userId, Long roomId) {
        UserRoomJpaEntity userRoomJpaEntity = userRoomJpaRepository.findByUserJpaEntity_UserIdAndRoomJpaEntity_RoomId(userId, roomId).orElseThrow(
                () -> new EntityNotFoundException(ErrorCode.USER_ROOM_NOT_FOUND)
        );

        return userRoomMapper.toDomainEntity(userRoomJpaEntity);
    }

    @Override
    public List<UserRoom> findAllByRoomId(Long roomId) {
        return userRoomJpaRepository.findAllByRoomJpaEntity_RoomId(roomId).stream()
                .map(userRoomMapper::toDomainEntity)
                .toList();
    }
}
