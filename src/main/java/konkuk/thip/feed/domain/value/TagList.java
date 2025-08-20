package konkuk.thip.feed.domain.value;

import konkuk.thip.common.exception.InvalidStateException;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

import static konkuk.thip.common.exception.code.ErrorCode.*;

public final class TagList extends AbstractList<Tag> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final int MAX_SIZE = 5;

    private final List<Tag> tags;

    private TagList(List<Tag> tags) {
        this.tags = new ArrayList<>(tags == null ? List.of() : tags);
        validate();
    }

    public static TagList of(List<Tag> tags) {
        return new TagList(tags);
    }

    public static TagList empty() {
        return new TagList(List.of());
    }

    // todo presignedURL 도입 전 임시 유효성 검증 메서드
    public static void validateTags(List<Tag> tags) {
        if (tags.size() > MAX_SIZE) {
            throw new InvalidStateException(TAG_LIST_SIZE_OVERFLOW,
                    new IllegalArgumentException("태그는 최대 " + MAX_SIZE + "개까지 입력할 수 있습니다."));
        }
        long distinctCount = tags.stream().distinct().count();
        if (distinctCount != tags.size()) {
            throw new InvalidStateException(TAG_SHOULD_BE_UNIQUE);
        }
    }

    private void validate() {
        validateTags(tags);
    }

    public List<Tag> toUnmodifiableList() {
        return List.copyOf(tags);
    }

    // AbstractList 구현
    @Override
    public Tag get(int index) {
        return tags.get(index);
    }

    @Override
    public int size() {
        return tags.size();
    }

    @Override
    public boolean add(Tag tag) {
        boolean res = tags.add(tag);
        validate();
        return res;
    }

    @Override
    public boolean addAll(Collection<? extends Tag> c) {
        boolean res = tags.addAll(c);
        validate();
        return res;
    }

    @Override
    public Tag set(int index, Tag element) {
        Tag prev = tags.set(index, element);
        validate();
        return prev;
    }

    @Override
    public void add(int index, Tag element) {
        tags.add(index, element);
        validate();
    }

    @Override
    public Tag remove(int index) {
        return tags.remove(index);
    }

    @Override
    public void clear() {
        tags.clear();
    }

    @Override
    public Iterator<Tag> iterator() {
        return tags.iterator();
    }
}
