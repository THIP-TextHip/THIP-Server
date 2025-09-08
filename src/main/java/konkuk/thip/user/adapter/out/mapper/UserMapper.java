package konkuk.thip.user.adapter.out.mapper;

import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.domain.value.UserRole;
import konkuk.thip.user.domain.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserJpaEntity toJpaEntity(User user) {
        return UserJpaEntity.builder()
                .nickname(user.getNickname())
                .nicknameUpdatedAt(user.getNicknameUpdatedAt())
                .role(UserRole.from(user.getUserRole()))
                .oauth2Id(user.getOauth2Id())
                .followerCount(user.getFollowerCount())
                .alias(user.getAlias())
                .build();
    }

    public User toDomainEntity(UserJpaEntity userJpaEntity) {
        return User.builder()
                .id(userJpaEntity.getUserId())
                .nickname(userJpaEntity.getNickname())
                .nicknameUpdatedAt(userJpaEntity.getNicknameUpdatedAt())
                .userRole(userJpaEntity.getRole().getType())
                .oauth2Id(userJpaEntity.getOauth2Id())
                .followerCount(userJpaEntity.getFollowerCount())
                .alias(userJpaEntity.getAlias())
                .createdAt(userJpaEntity.getCreatedAt())
                .modifiedAt(userJpaEntity.getModifiedAt())
                .status(userJpaEntity.getStatus())
                .build();
    }
}
