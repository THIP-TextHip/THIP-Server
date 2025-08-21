package konkuk.thip.feed.domain.value;

import konkuk.thip.common.exception.InvalidStateException;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static konkuk.thip.common.exception.code.ErrorCode.CONTENT_LIST_SIZE_OVERFLOW;
import static konkuk.thip.common.exception.code.ErrorCode.CONTENT_NOT_FOUND;

public final class ContentList extends AbstractList<String> implements Serializable {

    @Serial // 클래스 버전 관리를 위한 직렬화 ID
    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_MAX_SIZE = 3;

    private final List<String> contents;

    private final int maxSize;

    private ContentList(List<String> values, int maxSize) {
        this.maxSize = maxSize;
        this.contents = new ArrayList<>(values == null ? List.of() : values);
        validate();
    }

    public static ContentList of(List<String> values) {
        return new ContentList(values, DEFAULT_MAX_SIZE);
    }

    public static ContentList of(List<String> values, int maxSize) {
        return new ContentList(new ArrayList<>(values), maxSize);
    }

    public static ContentList empty() {
        return new ContentList(List.of(), DEFAULT_MAX_SIZE);
    }

    public static void validateImageCount(int size) {
        if (size > DEFAULT_MAX_SIZE) {
            throw new InvalidStateException(CONTENT_LIST_SIZE_OVERFLOW);
        }
    }

    private void validate() {
        validateImageCount(contents.size());
        //todo 필요 시 URL 형식 검증 추가 가능
    }

    public void validateOwnImages(List<String> newImageUrls) {
        Set<String> myImageUrls = contents.stream()
                .filter(url -> url != null && !url.isBlank())
                .collect(Collectors.toSet());
        for (String url : newImageUrls) {
            if (!myImageUrls.contains(url)) {
                throw new InvalidStateException(CONTENT_NOT_FOUND, new IllegalArgumentException(url));
            }
        }
    }

    // AbstractList 구현 위임 구간
    @Override
    public String get(int index) {
        return contents.get(index);
    }

    @Override
    public int size() {
        return contents.size();
    }

    @Override
    public String set(int index, String element) {
        String prev = contents.set(index, element);
        validate();
        return prev;
    }

    @Override
    public void add(int index, String element) {
        contents.add(index, element);
        validate();
    }

    @Override
    public String remove(int index) {
        return contents.remove(index);
    }

    @Override
    public boolean add(String s) {
        boolean res = contents.add(s);
        validate();
        return res;
    }

    @Override
    public boolean addAll(Collection<? extends String> c) {
        boolean res = contents.addAll(c);
        validate();
        return res;
    }

    @Override
    public boolean addAll(int index, Collection<? extends String> c) {
        boolean res = contents.addAll(index, c);
        validate();
        return res;
    }

    @Override
    public boolean remove(Object o) {
        return contents.remove(o);
    }

    @Override
    public void clear() {
        contents.clear();
    }

    @Override
    public Iterator<String> iterator() {
        return contents.iterator();
    }

    // 값 동등성 보장
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContentList that)) return false;
        return this.contents.equals(that.contents);
    }

    @Override
    public int hashCode() {
        return contents.hashCode();
    }

    // 불변 리스트 스냅샷을 원할 때 사용
    public List<String> toUnmodifiableList() {
        return List.copyOf(contents);
    }
}