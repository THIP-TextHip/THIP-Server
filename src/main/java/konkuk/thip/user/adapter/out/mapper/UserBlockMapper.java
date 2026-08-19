package konkuk.thip.user.adapter.out.mapper;

import konkuk.thip.user.adapter.out.jpa.UserBlockJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.domain.UserBlock;
import org.springframework.stereotype.Component;

@Component
public class UserBlockMapper {

    public UserBlockJpaEntity toJpaEntity(UserJpaEntity userJpaEntity, UserJpaEntity blockedUserJpaEntity) {
        return UserBlockJpaEntity.builder()
                .userJpaEntity(userJpaEntity)
                .blockedUserJpaEntity(blockedUserJpaEntity)
                .build();
    }

    public UserBlock toDomainEntity(UserBlockJpaEntity userBlockJpaEntity) {
        return UserBlock.builder()
                .id(userBlockJpaEntity.getBlockId())
                .userId(userBlockJpaEntity.getUserJpaEntity().getUserId())
                .blockedUserId(userBlockJpaEntity.getBlockedUserJpaEntity().getUserId())
                .createdAt(userBlockJpaEntity.getCreatedAt())
                .modifiedAt(userBlockJpaEntity.getModifiedAt())
                .status(userBlockJpaEntity.getStatus())
                .build();
    }
}
