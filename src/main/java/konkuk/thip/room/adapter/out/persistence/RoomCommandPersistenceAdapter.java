package konkuk.thip.room.adapter.out.persistence;

import konkuk.thip.room.adapter.out.mapper.RoomMapper;
import konkuk.thip.room.application.port.out.RoomCommandPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RoomCommandPersistenceAdapter implements RoomCommandPort {

    private final RoomJpaRepository jpaRepository;
    private final RoomMapper userMapper;

}
