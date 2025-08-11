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
    SCIENCE_IT("과학·IT", "https://thip-bucket.s3.ap-northeast-2.amazonaws.com/group_science.png"),
    LITERATURE("문학", "https://thip-bucket.s3.ap-northeast-2.amazonaws.com/group_literature.png"),
    ART("예술", "https://thip-bucket.s3.ap-northeast-2.amazonaws.com/group_art.png"),
    SOCIAL_SCIENCE("사회과학", "https://thip-bucket.s3.ap-northeast-2.amazonaws.com/group_sociology.png"),
    HUMANITY("인문학", "https://thip-bucket.s3.ap-northeast-2.amazonaws.com/group_humanities.png");

    private final String value;
    private final String imageUrl;

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
}
