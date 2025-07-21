package konkuk.thip.common.util;

@FunctionalInterface
public interface CursorExtractor<T> {
    String extractCursor(T lastElement);
}
