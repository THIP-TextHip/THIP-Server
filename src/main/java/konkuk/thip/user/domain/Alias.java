package konkuk.thip.user.domain;

import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.common.exception.code.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.Supplier;

@Getter
@RequiredArgsConstructor
public enum Alias {
    WRITER("문학가", "#A0F8E8", "/profile_literature.png"),
    SCIENTIST("과학자", "#C8A5FF", "/profile_science.png"),
    SOCIOLOGIST("사회학자", "#FDB770", "/profile_sociology.png"),
    ARTIST("예술가", "#FF8BAC", "/profile_art.png"),
    PHILOSOPHER("철학자", "#A1D5FF", "/profile_humanities.png");

    private final String value;
    private final String color;
    private final String imageUrl;

    private static volatile Supplier<String> baseUrlSupplier;

    public static void registerBaseUrlSupplier(Supplier<String> supplier) {
        baseUrlSupplier = supplier;
    }

    public static Alias from(String value) {
        for (Alias alias : Alias.values()) {
            if (alias.value.equals(value)) {
                return alias;
            }
        }
        throw new InvalidStateException(ErrorCode.ALIAS_NAME_NOT_MATCH);
    }

    // aws bucket base url + 파일명
    public String getImageUrl() {
       return baseUrlSupplier.get() + imageUrl;
    }
}
