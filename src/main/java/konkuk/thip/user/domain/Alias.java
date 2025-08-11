package konkuk.thip.user.domain;

import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.common.exception.code.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Alias {
    WRITER("문학가", "#A0F8E8", "https://thip-bucket.s3.ap-northeast-2.amazonaws.com/profile_literature.png"),
    SCIENTIST("과학자", "#C8A5FF", "https://thip-bucket.s3.ap-northeast-2.amazonaws.com/profile_science.png"),
    SOCIOLOGIST("사회학자", "#FDB770", "https://thip-bucket.s3.ap-northeast-2.amazonaws.com/profile_sociology.png"),
    ARTIST("예술가", "#FF8BAC", "https://thip-bucket.s3.ap-northeast-2.amazonaws.com/profile_art.png"),
    PHILOSOPHER("철학자", "#A1D5FF", "https://thip-bucket.s3.ap-northeast-2.amazonaws.com/profile_humanities.png");

    private final String value;
    private final String color;
    private final String imageUrl;

    public static Alias from(String value) {
        for (Alias alias : Alias.values()) {
            if (alias.value.equals(value)) {
                return alias;
            }
        }
        throw new InvalidStateException(ErrorCode.ALIAS_NAME_NOT_MATCH);
    }
}
