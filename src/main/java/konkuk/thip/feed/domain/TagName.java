package konkuk.thip.feed.domain;

import konkuk.thip.common.exception.InvalidStateException;
import lombok.Getter;

import static konkuk.thip.common.exception.code.ErrorCode.TAG_NAME_NOT_MATCH;

@Getter
public enum TagName {
    BOOK_RECOMMEND("책추천"),
    TODAYS_BOOK("오늘의책"),
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

    private final String tag;

    TagName(String tag) {
        this.tag = tag;
    }

    public static TagName from(String tag) {
        for (TagName tagName : TagName.values()) {
            if (tagName.tag.equalsIgnoreCase(tag)) {
                return tagName;
            }
        }
        throw new InvalidStateException(TAG_NAME_NOT_MATCH);
    }
}
