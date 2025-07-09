package konkuk.thip.room.adapter.out.persistence;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum CategoryName {

    /**
     * DB에 저장되어 있는 모든 카테고리들의 이름
     * TODO : DB에서 value를 통해 카테고리를 조회하는것보다 id로 조회하는게 성능상 좋으니, id 값도 같이 보관 ??
     */
    SCIENCE_IT("과학/IT"),
    Literature("문학"),
    ART("예술"),
    SOCIAL_SCIENCE("사회과확"),
    HUMANITY("인문학");

    private final String value;

    public static CategoryName from(String value) {
        return Arrays.stream(CategoryName.values())
                .filter(categoryName -> categoryName.getValue().equals(value))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException("현재 카테고리 이름 : " + value)
                );
    }
}
