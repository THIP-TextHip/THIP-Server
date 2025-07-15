package konkuk.thip.user.adapter.out.mapper;

import konkuk.thip.user.adapter.out.jpa.FollowingJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.domain.Following;
import org.springframework.stereotype.Component;

@Component
public class FollowingMapper {

    public FollowingJpaEntity toJpaEntity(UserJpaEntity followerUserJpaEntity, UserJpaEntity followingUserJpaEntity) {
        return FollowingJpaEntity.builder()
                .followerUserJpaEntity(followerUserJpaEntity)
                .followingUserJpaEntity(followingUserJpaEntity)
                .build();
    }

    public Following toDomainEntity(FollowingJpaEntity followingJpaEntity) {
        return Following.builder()
                .id(followingJpaEntity.getFollowingId())
                .followerUserId(followingJpaEntity.getFollowerUserJpaEntity().getUserId())
                .followingUserId(followingJpaEntity.getFollowingUserJpaEntity().getUserId())
                .createdAt(followingJpaEntity.getCreatedAt())
                .modifiedAt(followingJpaEntity.getModifiedAt())
                .status(followingJpaEntity.getStatus())
                .build();
    }
}
