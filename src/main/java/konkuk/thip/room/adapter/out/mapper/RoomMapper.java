package konkuk.thip.room.adapter.out.mapper;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.room.adapter.out.jpa.CategoryJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.domain.Category;
import konkuk.thip.room.domain.Room;
import org.springframework.stereotype.Component;

@Component
public class RoomMapper {

    public RoomJpaEntity toJpaEntity(Room room, BookJpaEntity bookJpaEntity, CategoryJpaEntity categoryJpaEntity) {
        return RoomJpaEntity.builder()
                .title(room.getTitle())
                .description(room.getDescription())
                .isPublic(room.isPublic())
                .password(room.getHashedPassword())
                .roomPercentage(room.getRoomPercentage())
                .startDate(room.getStartDate())
                .endDate(room.getEndDate())
                .recruitCount(room.getRecruitCount())
                .memberCount(room.getMemberCount())
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
                .hashedPassword(roomJpaEntity.getPassword())
                .roomPercentage(roomJpaEntity.getRoomPercentage())
                .startDate(roomJpaEntity.getStartDate())
                .endDate(roomJpaEntity.getEndDate())
                .recruitCount(roomJpaEntity.getRecruitCount())
                .memberCount(roomJpaEntity.getMemberCount())
                .bookId(roomJpaEntity.getBookJpaEntity().getBookId())
                .category(Category.from(roomJpaEntity.getCategoryJpaEntity().getValue()))
                .createdAt(roomJpaEntity.getCreatedAt())
                .modifiedAt(roomJpaEntity.getModifiedAt())
                .status(roomJpaEntity.getStatus())
                .build();
    }
}
