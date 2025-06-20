package konkuk.thip.domain.room.adapter.out.persistence;

import konkuk.thip.domain.room.adapter.out.mapper.RoomMapper;
import konkuk.thip.domain.room.application.port.out.RoomCommandPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RoomCommandPersistenceAdapter implements RoomCommandPort {

    private final RoomJpaRepository jpaRepository;
    private final RoomMapper userMapper;

}
