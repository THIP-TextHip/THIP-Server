package konkuk.thip.book.adapter.out.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import konkuk.thip.book.application.port.in.dto.BookMostSearchResult;
import konkuk.thip.book.application.port.out.BookRedisQueryPort;
import konkuk.thip.common.exception.ExternalApiException;
import konkuk.thip.common.exception.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BookQueryRedisAdapter implements BookRedisQueryPort {

    private final RedisTemplate<String, String> redisTemplate;
    private final DateTimeFormatter DAILY_KEY_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    @Value("${app.redis.search-count-prefix}")
    private String searchCountPrefix;

    @Value("${app.redis.search-rank-prefix}")
    private String searchRankPrefix;

    @Value("${app.redis.search-rank-detail-prefix}")
    private String searchRankDetailPrefix;

    private final ObjectMapper objectMapper;

    @Override
    public List<Map.Entry<String, Double>> getBookSearchRank(LocalDate date, int topN) {
        return getTopNFromZSet(searchRankPrefix, date, topN);
    }

    @Override
    public List<Map.Entry<String, Double>> getBookSearchCountTopN(LocalDate date, int topN) {
        return getTopNFromZSet(searchCountPrefix, date, topN);
    }

    private List<Map.Entry<String, Double>> getTopNFromZSet(String prefix, LocalDate date, int topN) {
        String dateStr = date.format(DAILY_KEY_FORMATTER);
        String redisKey = prefix + dateStr;
        Set<ZSetOperations.TypedTuple<String>> topNSet = redisTemplate.opsForZSet()
                .reverseRangeWithScores(redisKey, 0, topN - 1);

        if (topNSet == null) return Collections.emptyList();
        return topNSet.stream()
                .map(tuple -> Map.entry(tuple.getValue(), tuple.getScore()))
                .collect(Collectors.toList());
    }

    @Override
    public List<BookMostSearchResult.BookRankInfo> getYesterdayBookRankInfos(LocalDate date) {
        String redisKey = searchRankDetailPrefix + date.format(DAILY_KEY_FORMATTER);
        String json = redisTemplate.opsForValue().get(redisKey);

        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(
                    json,
                    new TypeReference<List<BookMostSearchResult.BookRankInfo>>() {}
            );
        } catch (JsonProcessingException e) {
            throw new ExternalApiException(ErrorCode.JSON_PROCESSING_ERROR);
        }
    }

}
