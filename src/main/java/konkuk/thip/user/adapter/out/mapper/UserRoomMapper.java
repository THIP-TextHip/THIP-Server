package konkuk.thip.user.adapter.out.mapper;

import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserRoomJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserRoomRole;
import konkuk.thip.user.domain.UserRoom;
import org.springframework.stereotype.Component;

@Component
public class UserRoomMapper {

    public UserRoomJpaEntity toJpaEntity(UserRoom userRoom, UserJpaEntity userJpaEntity, RoomJpaEntity roomJpaEntity) {
        return UserRoomJpaEntity.builder()
                .currentPage(userRoom.getCurrentPage())
                .userPercentage(userRoom.getUserPercentage())
                .userRoomRole(UserRoomRole.from(userRoom.getUserRoomRole()))
                .userJpaEntity(userJpaEntity)
                .roomJpaEntity(roomJpaEntity)
                .build();
    }

    public UserRoom toDomainEntity(UserRoomJpaEntity userRoomJpaEntity) {
        return UserRoom.builder()
                .id(userRoomJpaEntity.getUserRoomId())
                .currentPage(userRoomJpaEntity.getCurrentPage())
                .userPercentage(userRoomJpaEntity.getUserPercentage())
                .userRoomRole(userRoomJpaEntity.getUserRoomRole().getType())
                .userId(userRoomJpaEntity.getUserJpaEntity().getUserId())
                .roomId(userRoomJpaEntity.getRoomJpaEntity().getRoomId())
                .createdAt(userRoomJpaEntity.getCreatedAt())
                .modifiedAt(userRoomJpaEntity.getModifiedAt())
                .status(userRoomJpaEntity.getStatus())
                .build();
    }
}
