package konkuk.thip.feed.domain.value;

import konkuk.thip.common.exception.InvalidStateException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static konkuk.thip.common.exception.code.ErrorCode.TAG_NAME_NOT_MATCH;

@Getter
@RequiredArgsConstructor
public enum Tag {
    // 문학
    KOREAN_NOVEL("한국소설"),
    FOREIGN_NOVEL("외국소설"),
    CLASSIC_LITERATURE("고전문학"),
    ESSAY("에세이"),
    POETRY("시"),
    PLAY("희곡"),
    DETECTIVE_NOVEL("추리소설"),
    FANTASY_NOVEL("판타지소설"),
    ROMANCE_NOVEL("로맨스소설"),
    LITERARY_THEORY("문학이론"),

    // 과학·IT
    GENERAL_SCIENCE("과학일반"),
    PHYSICS("물리학"),
    CHEMISTRY("화학"),
    BIOLOGY("생명과학"),
    ASTRONOMY("천문학"),
    EARTH_SCIENCE("지구과학"),
    MATHEMATICS("수학"),
    GENERAL_ENGINEERING("공학일반"),
    COMPUTER_ENGINEERING("컴퓨터공학"),
    PROGRAMMING("프로그래밍"),
    IT_GENERAL("IT일반"),

    // 사회과학
    SOCIOLOGY("사회학"),
    LAW("법률"),
    GENERAL_POLITICS("정치일반"),
    POLITICAL_SCIENCE("정치학"),
    ECONOMICS("경제학"),
    BUSINESS_ADMIN("경영학"),
    JURISPRUDENCE("법학"),
    EDUCATION("교육학"),
    PSYCHOLOGY("심리학"),
    MEDIA("언론미디어"),
    INTERNATIONAL_RELATIONS("국제관계"),
    SOCIAL_ISSUES("사회문제"),
    MARKETING("마케팅"),
    INVESTMENT("재테크"),
    STARTUP("창업"),
    GENERAL_ECONOMY("경제일반"),

    // 인문학
    PHILOSOPHY("철학"),
    HISTORY("역사"),
    RELIGION("종교"),
    CLASSICS("고전"),
    LINGUISTICS("언어학"),
    CULTURAL_ANTHROPOLOGY("문화인류학"),
    HUMANISTIC_ESSAY("인문에세이"),
    EASTERN_PHILOSOPHY("동양철학"),
    WESTERN_PHILOSOPHY("서양철학"),
    WORLD_HISTORY("세계사"),
    KOREAN_HISTORY("한국사"),
    HISTORICAL_ESSAY("역사에세이"),
    ANCIENT_HISTORY("고대사"),
    MODERN_HISTORY("근현대사"),

    // 예술
    ART("미술"),
    MUSIC("음악"),
    MOVIE("영화"),
    PHOTO("사진"),
    DANCE("무용"),
    THEATER("연극"),
    DESIGN("디자인"),
    ARCHITECTURE("건축"),
    GENERAL_ART("예술일반");

    private final String value;

    public static Tag from(String value) {
        for (Tag tagName : Tag.values()) {
            if (tagName.value.equalsIgnoreCase(value)) {
                return tagName;
            }
        }
        throw new InvalidStateException(TAG_NAME_NOT_MATCH);
    }

    public static List<Tag> fromList(List<String> values) {
        if (values == null || values.isEmpty()) return List.of();

        List<Tag> tags = new ArrayList<>();
        List<String> invalidValues = new ArrayList<>();

        for (String value : values) {
            try {
                tags.add(Tag.from(value));
            } catch (InvalidStateException e) {
                invalidValues.add(value);
            }
        }

        if (!invalidValues.isEmpty()) {
            String message = "다음 태그 이름이 유효하지 않습니다: " + String.join(", ", invalidValues);
            throw new InvalidStateException(TAG_NAME_NOT_MATCH, new IllegalArgumentException(message));
        }

        return tags;
    }

}
