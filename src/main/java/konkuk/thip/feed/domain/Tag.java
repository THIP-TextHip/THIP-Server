package konkuk.thip.feed.domain;

import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.room.domain.Category;
import lombok.Getter;

import static konkuk.thip.common.exception.code.ErrorCode.TAG_NAME_NOT_MATCH;

@Getter
public enum Tag {
    BOOK_RECOMMEND("책추천", Category.LITERATURE),
    TODAYS_BOOK("오늘의책", Category.LITERATURE),
    READING_LOG("독서기록", Category.LITERATURE),
    BOOK_REVIEW("책리뷰", Category.LITERATURE),
    QUOTE("책속한줄", Category.LITERATURE),
    BOOK_REPORT("독후감", Category.LITERATURE),
    LIFE_BOOK("내인생책", Category.LITERATURE),
    RE_READ("다시읽고싶은책", Category.LITERATURE),
    BOOK_TALK("북토크", Category.LITERATURE),
    BOOKSTAGRAM("책스타그램", Category.LITERATURE),
    NOVEL("소설추천", Category.LITERATURE),
    SELF_IMPROVEMENT("자기계발서", Category.HUMANITY),
    PHILOSOPHY("인문학책", Category.HUMANITY),
    SCIENCE("과학책", Category.SCIENCE_IT),
    ECONOMY("경제책", Category.SOCIAL_SCIENCE);

    private final String value;
    private final Category category;

    Tag(String value, Category category) {
        this.value = value;
        this.category = category;
    }

    public static Tag from(String value) {
        for (Tag tagName : Tag.values()) {
            if (tagName.value.equalsIgnoreCase(value)) {
                return tagName;
            }
        }
        throw new InvalidStateException(TAG_NAME_NOT_MATCH);
    }
}
