package konkuk.thip.room.adapter.out.mapper;

import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.domain.value.RoomParticipantRole;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomParticipantJpaEntity;
import konkuk.thip.room.domain.RoomParticipant;
import org.springframework.stereotype.Component;

@Component
public class RoomParticipantMapper {

    public RoomParticipantJpaEntity toJpaEntity(RoomParticipant roomParticipant, UserJpaEntity userJpaEntity, RoomJpaEntity roomJpaEntity) {
        return RoomParticipantJpaEntity.builder()
                .currentPage(roomParticipant.getCurrentPage())
                .userPercentage(roomParticipant.getUserPercentage())
                .roomParticipantRole(RoomParticipantRole.from(roomParticipant.getRoomParticipantRole()))
                .userJpaEntity(userJpaEntity)
                .roomJpaEntity(roomJpaEntity)
                .build();
    }

    public RoomParticipant toDomainEntity(RoomParticipantJpaEntity roomParticipantJpaEntity) {
        return RoomParticipant.builder()
                .id(roomParticipantJpaEntity.getRoomParticipantId())
                .currentPage(roomParticipantJpaEntity.getCurrentPage())
                .userPercentage(roomParticipantJpaEntity.getUserPercentage())
                .roomParticipantRole(roomParticipantJpaEntity.getRoomParticipantRole().getType())
                .userId(roomParticipantJpaEntity.getUserJpaEntity().getUserId())
                .roomId(roomParticipantJpaEntity.getRoomJpaEntity().getRoomId())
                .createdAt(roomParticipantJpaEntity.getCreatedAt())
                .modifiedAt(roomParticipantJpaEntity.getModifiedAt())
                .status(roomParticipantJpaEntity.getStatus())
                .build();
    }
}
