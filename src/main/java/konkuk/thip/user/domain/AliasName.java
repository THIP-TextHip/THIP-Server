package konkuk.thip.user.domain;

import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.common.exception.code.ErrorCode;
import lombok.Getter;

@Getter
public enum AliasName {
    WRITER("문학가"),
    SCIENTIST("과학자"),
    SOCIOLOGIST("사회학자"),
    ARTIEST("예술가"),
    PHILOSOPHER("철학자");

    private final String value;

    AliasName(String value) {
        this.value = value;
    }

    public static AliasName from(String aliasName) {
        for (AliasName name : AliasName.values()) {
            if (name.value.equals(aliasName)) {
                return name;
            }
        }
        throw new InvalidStateException(ErrorCode.ALIAS_NAME_NOT_MATCH);
    }
}
