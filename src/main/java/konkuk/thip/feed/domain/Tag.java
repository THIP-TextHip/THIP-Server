package konkuk.thip.feed.domain;

import konkuk.thip.common.exception.InvalidStateException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static konkuk.thip.common.exception.code.ErrorCode.TAG_NAME_NOT_MATCH;

@Getter
@RequiredArgsConstructor
public enum Tag {
    BOOK_RECOMMEND("책추천"),
    TODAY_BOOK("오늘의책"),
    READING_LOG("독서기록"),
    BOOK_REVIEW("책리뷰"),
    QUOTE("책속한줄"),
    BOOK_REPORT("독후감"),
    LIFE_BOOK("내인생책"),
    RE_READ("다시읽고싶은책"),
    BOOK_TALK("북토크"),
    BOOKSTAGRAM("책스타그램"),
    NOVEL("소설추천"),
    SELF_IMPROVEMENT("자기계발서"),
    PHILOSOPHY("인문학책"),
    SCIENCE("과학책"),
    ECONOMY("경제책");

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
            String message = String.format(
                    "다음 태그 이름이 유효하지 않습니다: " + invalidValues
            );
            throw new InvalidStateException( TAG_NAME_NOT_MATCH,
                    new IllegalArgumentException(message)
            );
        }

        return tags;
    }

}
