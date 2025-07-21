package konkuk.thip.common.util;

import java.util.List;

public record CursorBasedList<T>(
        List<T> contents,
        String nextCursor,
        boolean hasNext
) {
    public static <T> CursorBasedList<T> of(List<T> contents, String nextCursor) {
        return new CursorBasedList<>(
                contents,
                nextCursor,
                nextCursor != null
        );
    }
}
