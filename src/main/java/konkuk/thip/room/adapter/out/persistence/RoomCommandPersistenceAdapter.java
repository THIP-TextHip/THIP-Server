package konkuk.thip.room.adapter.out.persistence;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.room.adapter.out.jpa.CategoryJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.adapter.out.mapper.RoomMapper;
import konkuk.thip.room.adapter.out.persistence.repository.category.CategoryJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.room.application.port.out.RoomCommandPort;
import konkuk.thip.room.domain.Room;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static konkuk.thip.common.exception.code.ErrorCode.*;

@Repository
@RequiredArgsConstructor
public class RoomCommandPersistenceAdapter implements RoomCommandPort {

    private final RoomJpaRepository roomJpaRepository;
    private final BookJpaRepository bookJpaRepository;
    private final CategoryJpaRepository categoryJpaRepository;

    private final RoomMapper roomMapper;

    @Override
    public Room findById(Long id) {
        RoomJpaEntity roomJpaEntity = roomJpaRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(ROOM_NOT_FOUND)
        );
        return roomMapper.toDomainEntity(roomJpaEntity);
    }

    @Override
    public Long save(Room room) {
        BookJpaEntity bookJpaEntity = bookJpaRepository.findById(room.getBookId()).orElseThrow(
                () -> new EntityNotFoundException(BOOK_NOT_FOUND)
        );

        CategoryJpaEntity categoryJpaEntity = categoryJpaRepository.findByValue(room.getCategory().getValue()).orElseThrow(
                () -> new EntityNotFoundException(CATEGORY_NOT_FOUND)
        );

        RoomJpaEntity roomJpaEntity = roomMapper.toJpaEntity(room, bookJpaEntity, categoryJpaEntity);
        return roomJpaRepository.save(roomJpaEntity).getRoomId();
    }

    @Override
    public void updateMemberCount(Room room) {
        RoomJpaEntity roomJpaEntity = roomJpaRepository.findById(room.getId()).orElseThrow(
                () -> new EntityNotFoundException(ROOM_NOT_FOUND)
        );

        roomJpaEntity.updateMemberCount(room.getMemberCount());

        roomJpaRepository.save(roomJpaEntity);
    }
}
