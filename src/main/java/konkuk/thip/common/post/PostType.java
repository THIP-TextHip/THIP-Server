package konkuk.thip.common.post;

import konkuk.thip.common.exception.InvalidStateException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

import static konkuk.thip.common.exception.code.ErrorCode.POST_TYPE_NOT_MATCH;

@Getter
@RequiredArgsConstructor
public enum PostType {

    FEED("FEED"),
    RECORD("RECORD"),
    VOTE("VOTE");

    private final String type;

    public static PostType from(String type) {
        return Arrays.stream(PostType.values())
                .filter(p -> p.getType().equalsIgnoreCase(type))
                .findFirst()
                .orElseThrow(() ->
                        new InvalidStateException(POST_TYPE_NOT_MATCH)
                );
    }
}
