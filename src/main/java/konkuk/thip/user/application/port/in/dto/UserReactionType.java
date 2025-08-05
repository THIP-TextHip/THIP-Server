package konkuk.thip.user.application.port.in.dto;

import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.common.exception.code.ErrorCode;
import lombok.Getter;

@Getter
public enum UserReactionType {
    LIKE("LIKE", "좋아요"),
    COMMENT("COMMENT", "댓글"),
    BOTH("BOTH", "좋아요와 댓글");

    private final String type;
    private final String label;

    UserReactionType(String type, String label) {
        this.type = type;
        this.label = label;
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
