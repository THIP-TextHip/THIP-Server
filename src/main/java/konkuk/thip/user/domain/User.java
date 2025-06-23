package konkuk.thip.user.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class User extends BaseDomainEntity {

    private Long id;

    private String nickname;

    private String imageUrl;

    private String userRole;

    private Long aliasId;

}
