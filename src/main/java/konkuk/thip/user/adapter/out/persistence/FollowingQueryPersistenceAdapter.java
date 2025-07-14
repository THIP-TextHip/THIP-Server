package konkuk.thip.user.adapter.out.persistence;

import konkuk.thip.user.adapter.out.mapper.FollowingMapper;
import konkuk.thip.user.application.port.out.FollowingQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class FollowingQueryPersistenceAdapter implements FollowingQueryPort {

    private final FollowingJpaRepository followingJpaRepository;
    private final FollowingMapper followingMapper;

    @Override
    public Map<Long, Integer> countByFollowingUserIds(List<Long> userIds) {
        return followingJpaRepository.countByFollowingUserIds(userIds);
    }
}
