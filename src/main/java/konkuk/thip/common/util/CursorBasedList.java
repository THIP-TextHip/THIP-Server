package konkuk.thip.common.util;

import java.util.List;

public record CursorBasedList<T>(
        List<T> contents,
        String nextCursor,
        boolean hasNext
) {
    public static <T> CursorBasedList<T> of(List<T> queryList, int size, CursorExtractor<T> extractor) {
        boolean hasNext = queryList.size() > size;
        List<T> contents = hasNext ? queryList.subList(0, size) : queryList;
        String nextCursor = hasNext ? extractor.extractCursor(contents.get(size - 1)) : null;
        return new CursorBasedList<>(contents, nextCursor, hasNext);
    }
}
