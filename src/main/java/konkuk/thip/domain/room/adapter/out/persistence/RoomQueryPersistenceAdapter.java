package konkuk.thip.domain.room.adapter.out.persistence;

import konkuk.thip.domain.room.adapter.out.mapper.RoomMapper;
import konkuk.thip.domain.room.application.port.out.RoomQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RoomQueryPersistenceAdapter implements RoomQueryPort {

    private final RoomJpaRepository jpaRepository;
    private final RoomMapper userMapper;

}
