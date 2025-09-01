package konkuk.thip.common.util;

import lombok.Getter;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

@Getter
public class Cursor {

    private static final String SPLIT_DELIMITER = "\\|";
    private static final String JOIN_DELIMITER = "|";
    private static final int DEFAULT_PAGE_SIZE = 10;

    private final List<String> rawCursorList;
    private final int pageSize;
    private final boolean isFirstRequest;

    // 인코딩용 생성자 (pageSize는 default 사용)
    public Cursor(List<String> rawCursorList) {
        this(rawCursorList, DEFAULT_PAGE_SIZE);
    }

    private Cursor(List<String> rawCursorList, int pageSize) {
        this.rawCursorList = rawCursorList;
        this.pageSize = pageSize;
        this.isFirstRequest = rawCursorList.isEmpty();
    }

    // 디코딩을 위한 정적 팩토리 메서드
    public static Cursor from(String encoded, int pageSize) {
        if (encoded == null) {
            return new Cursor(List.of(), pageSize);     // 빈 커서 생성
        }

        String decoded = URLDecoder.decode(encoded, StandardCharsets.UTF_8);

        if (!decoded.contains(JOIN_DELIMITER)) {
            return new Cursor(List.of(decoded), pageSize);      // 단일 커서
        }

        List<String> parts = Arrays.asList(decoded.split(SPLIT_DELIMITER));
        return new Cursor(parts, pageSize);     // 복합 커서
    }

    public String toEncodedString() {
        if (rawCursorList.size() == 1) {        // 단일 커서
            return URLEncoder.encode(rawCursorList.get(0), StandardCharsets.UTF_8);
        }

        // 복합 커서
        String raw = String.join(JOIN_DELIMITER, rawCursorList);
        return URLEncoder.encode(raw, StandardCharsets.UTF_8);
    }

    public LocalDateTime getLocalDateTime(int index) {
        return getAs(index, LocalDateTime::parse, "LocalDateTime");
    }

    public LocalDate getLocalDate(int index) {
        return getAs(index, LocalDate::parse, "LocalDate");
    }

    public Long getLong(int index) {
        return getAs(index, Long::parseLong, "Long");
    }

    public Integer getInteger(int index) {
        return getAs(index, Integer::parseInt, "Integer");
    }

    public Double getDouble(int index) {
        return getAs(index, Double::parseDouble, "Double");
    }

    public String getString(int index) {
        return get(index);
    }

    private String get(int index) {
        if (index < 0 || index >= rawCursorList.size()) {
            throw new IndexOutOfBoundsException("인덱스가 범위를 벗어났습니다: " + index);
        }
        return rawCursorList.get(index);
    }

    private <T> T getAs(int index, Function<String, T> parser, String typeName) {
        try {
            return parser.apply(get(index));
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    String.format("커서에서 %s 값을 파싱할 수 없습니다: '%s'", typeName, get(index)), e
            );
        }
    }
}