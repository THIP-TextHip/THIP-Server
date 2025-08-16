package konkuk.thip.roompost.adapter.out.mapper;

import konkuk.thip.roompost.adapter.out.jpa.RecordJpaEntity;
import konkuk.thip.roompost.domain.Record;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class RecordMapper {

    public RecordJpaEntity toJpaEntity(Record record, UserJpaEntity userJpaEntity, RoomJpaEntity roomJpaEntity) {
        return RecordJpaEntity.builder()
                .content(record.getContent())
                .likeCount(record.getLikeCount())
                .commentCount(record.getCommentCount())
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
                .likeCount(recordJpaEntity.getLikeCount())
                .commentCount(recordJpaEntity.getCommentCount())
                .createdAt(recordJpaEntity.getCreatedAt())
                .modifiedAt(recordJpaEntity.getModifiedAt())
                .status(recordJpaEntity.getStatus())
                .build();
    }
}
