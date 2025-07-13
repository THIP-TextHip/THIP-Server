package konkuk.thip.room.domain;

import konkuk.thip.common.exception.InvalidStateException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

import static konkuk.thip.common.exception.code.ErrorCode.CATEGORY_NOT_MATCH;

@Getter
@RequiredArgsConstructor
public enum Category {

    /**
     * DB에 저장되어 있는 모든 카테고리들의 이름
     * TODO : DB에서 value를 통해 카테고리를 조회하는것보다 id로 조회하는게 성능상 좋으니, id 값도 같이 보관 ??
     */
    SCIENCE_IT("과학/IT", "과학/IT_image"),
    LITERATURE("문학", "문학_image"),
    ART("예술", "예술_image"),
    SOCIAL_SCIENCE("사회과학", "사회과학_image"),
    HUMANITY("인문학", "인문학_image");

    private final String value;
    private final String imageUrl;

    public static Category from(String value) {
        return Arrays.stream(Category.values())
                .filter(categoryName -> categoryName.getValue().equals(value))
                .findFirst()
                .orElseThrow(
                        () -> new InvalidStateException(CATEGORY_NOT_MATCH)
                );
    }
}
