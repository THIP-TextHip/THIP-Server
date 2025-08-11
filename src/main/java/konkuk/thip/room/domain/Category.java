package konkuk.thip.room.domain;

import konkuk.thip.common.exception.InvalidStateException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.function.Supplier;

import static konkuk.thip.common.exception.code.ErrorCode.CATEGORY_NOT_MATCH;

@Getter
@RequiredArgsConstructor
public enum Category {

    /**
     * DB에 저장되어 있는 모든 카테고리들의 이름
     * TODO : DB에서 value를 통해 카테고리를 조회하는것보다 id로 조회하는게 성능상 좋으니, id 값도 같이 보관 ??
     */
    SCIENCE_IT("과학·IT", "/group_science.png"),
    LITERATURE("문학", "/group_literature.png"),
    ART("예술", "/group_art.png"),
    SOCIAL_SCIENCE("사회과학", "/group_sociology.png"),
    HUMANITY("인문학", "/group_humanities.png");

    private final String value;
    private final String imageUrl;

    private static volatile Supplier<String> baseUrlSupplier;

    public static void registerBaseUrlSupplier(Supplier<String> supplier) {
        baseUrlSupplier = supplier;
    }

    public static Category from(String value) {
        return Arrays.stream(Category.values())
                .filter(categoryName -> categoryName.getValue().equals(value.trim()))
                .findFirst()
                .orElseThrow(
                        () -> new InvalidStateException(CATEGORY_NOT_MATCH,
                                new IllegalArgumentException(
                                        String.format("존재하지 않는 카테고리입니다. value: %s", value)
                                ))
                );
    }

    // aws bucket base url + 파일명
    public String getImageUrl() {
        return baseUrlSupplier.get() + imageUrl;
    }
}
