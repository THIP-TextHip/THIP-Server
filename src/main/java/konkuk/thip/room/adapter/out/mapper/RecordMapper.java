package konkuk.thip.room.adapter.out.mapper;

import konkuk.thip.room.adapter.out.jpa.RecordJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.domain.Record;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class RecordMapper {

    public RecordJpaEntity toJpaEntity(Record record, UserJpaEntity userJpaEntity, RoomJpaEntity roomJpaEntity) {
        return RecordJpaEntity.builder()
                .content(record.getContent())
                .userJpaEntity(userJpaEntity)
                .page(record.getPage())
                .isOverview(record.isOverview())
                .roomJpaEntity(roomJpaEntity)
                .build();
    }

    public Record toDomainEntity(RecordJpaEntity recordJpaEntity) {
        return Record.builder()
                .id(recordJpaEntity.getPostId())
                .content(recordJpaEntity.getContent())
                .creatorId(recordJpaEntity.getUserJpaEntity().getUserId())
                .page(recordJpaEntity.getPage())
                .isOverview(recordJpaEntity.isOverview())
                .roomId(recordJpaEntity.getRoomJpaEntity().getRoomId())
                .createdAt(recordJpaEntity.getCreatedAt())
                .modifiedAt(recordJpaEntity.getModifiedAt())
                .status(recordJpaEntity.getStatus())
                .build();
    }
}
