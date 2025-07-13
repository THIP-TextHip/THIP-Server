package konkuk.thip.user.adapter.out.persistence;

import konkuk.thip.user.application.port.out.FollowingQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FollowingQueryPersistenceAdapter implements FollowingQueryPort {

    private final FollowingJpaRepository followingJpaRepository;

    @Override
    public int countByFollowingUserId(Long followingUserId) {
        return followingJpaRepository.countByFollowingUserJpaEntity_UserId(followingUserId);
    }
}
