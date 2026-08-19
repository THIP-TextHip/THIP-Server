package konkuk.thip.user.application.port.out.dto;

import com.querydsl.core.annotations.QueryProjection;
import konkuk.thip.user.domain.value.Alias;

import java.time.LocalDateTime;

public record BlockedUserQueryDto(Long userId,
                                  String nickname,
                                  String profileImageUrl,
                                  String aliasName,
                                  String aliasColor,
                                  Long blockId,
                                  LocalDateTime createdAt) {

    @QueryProjection
    public BlockedUserQueryDto(
            Long userId,
            String nickname,
            Alias userAlias,
            Long blockId,
            LocalDateTime createdAt
    ) {
        this(
                userId,
                nickname,
                userAlias.getImageUrl(),
                userAlias.getValue(),
                userAlias.getColor(),
                blockId,
                createdAt
        );
    }
}
