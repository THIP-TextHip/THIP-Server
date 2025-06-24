package konkuk.thip.room.adapter.out.mapper;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.jpa.CategoryJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.domain.Room;
import org.springframework.stereotype.Component;

@Component
public class RoomMapper {

    public RoomJpaEntity roomJpaEntity(Room room, BookJpaEntity bookJpaEntity, CategoryJpaEntity categoryJpaEntity) {
        return RoomJpaEntity.builder()
                .title(room.getTitle())
                .description(room.getDescription())
                .isPublic(room.isPublic())
                .password(room.getPassword())
                .roomPercentage(room.getRoomPercentage())
                .startDate(room.getStartDate())
                .endDate(room.getEndDate())
                .recruitCount(room.getRecruitCount())
                .bookJpaEntity(bookJpaEntity)
                .categoryJpaEntity(categoryJpaEntity)
                .build();
    }

    public Room toDomainEntity(RoomJpaEntity roomJpaEntity) {
        return Room.builder()
                .id(roomJpaEntity.getRoomId())
                .title(roomJpaEntity.getTitle())
                .description(roomJpaEntity.getDescription())
                .isPublic(roomJpaEntity.isPublic())
                .password(roomJpaEntity.getPassword())
                .roomPercentage(roomJpaEntity.getRoomPercentage())
                .startDate(roomJpaEntity.getStartDate())
                .endDate(roomJpaEntity.getEndDate())
                .recruitCount(roomJpaEntity.getRecruitCount())
                .bookId(roomJpaEntity.getBookJpaEntity().getBookId())
                .categoryId(roomJpaEntity.getCategoryJpaEntity().getCategoryId())
                .createdAt(roomJpaEntity.getCreatedAt())
                .modifiedAt(roomJpaEntity.getModifiedAt())
                .status(roomJpaEntity.getStatus())
                .build();
    }
}
