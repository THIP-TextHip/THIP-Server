package konkuk.thip.user.domain;

import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.common.exception.code.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Alias {
    WRITER("문학가", "문학_color", "문학_image"),
    SCIENTIST("과학자", "과학_color", "과학_image"),
    SOCIOLOGIST("사회학자", "사회과학_color", "사회과학_image"),
    ARTIST("예술가", "예술_color", "예술_image"),
    PHILOSOPHER("철학자", "철학_color", "철학_image");

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
