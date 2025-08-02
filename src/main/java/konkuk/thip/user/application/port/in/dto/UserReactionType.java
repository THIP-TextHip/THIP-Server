package konkuk.thip.user.application.port.in.dto;

import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.common.exception.code.ErrorCode;
import lombok.Getter;

@Getter
public enum UserReactionType {
    LIKE("LIKE"),
    COMMENT("COMMENT"),
    BOTH("BOTH");

    private final String type;

    UserReactionType(String type) {
        this.type = type;
    }

    public static UserReactionType from(String type) {
        for (UserReactionType reactionType : UserReactionType.values()) {
            if (reactionType.getType().equalsIgnoreCase(type)) {
                return reactionType;
            }
        }
        throw new InvalidStateException(ErrorCode.API_INVALID_PARAM,
            new IllegalArgumentException("유효하지 않은 사용자 반응 타입: " + type));
    }
}
