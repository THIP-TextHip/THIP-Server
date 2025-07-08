package konkuk.thip.book.adapter.out.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import konkuk.thip.book.application.port.in.dto.BookMostSearchResult;
import konkuk.thip.book.application.port.out.BookRedisCommandPort;
import konkuk.thip.book.application.port.out.BookRedisQueryPort;
import konkuk.thip.common.exception.ExternalApiException;
import konkuk.thip.common.exception.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static konkuk.thip.common.exception.code.ErrorCode.JSON_PROCESSING_ERROR;

@Component
@RequiredArgsConstructor
public class BookRedisAdapter implements BookRedisQueryPort, BookRedisCommandPort {

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

    @Override
    public void incrementBookSearchCount(String isbn, LocalDate date) {
        String redisKey = makeRedisKey(searchCountPrefix, date);
        redisTemplate.opsForZSet().incrementScore(redisKey, isbn, 1.0);
    }

    @Override
    public void saveBookSearchRank(List<String> isbns, List<Double> scores, LocalDate date) {
        String redisKey = makeRedisKey(searchRankPrefix, date);
        for (int i = 0; i < isbns.size(); i++) {
            redisTemplate.opsForZSet().add(redisKey, isbns.get(i), scores.get(i));
        }
        redisTemplate.expire(redisKey, Duration.ofDays(7));
    }

    @Override
    public void saveBookSearchRankDetail(List<BookMostSearchResult.BookRankInfo> bookRankDetails, LocalDate date) {
        String redisKey = makeRedisKey(searchRankDetailPrefix, date);
        String detailJson;
        try {
            detailJson = objectMapper.writeValueAsString(bookRankDetails);
        } catch (JsonProcessingException e) {
            throw new ExternalApiException(JSON_PROCESSING_ERROR);
        }
        redisTemplate.opsForValue().set(redisKey, detailJson);
    }

    @Override
    public void deleteBookSearchRank(LocalDate date) {
        deleteZSetKey(searchRankPrefix, date);
    }

    @Override
    public void deleteBookSearchCount(LocalDate date) { deleteZSetKey(searchCountPrefix, date); }

    @Override
    public void deleteBookSearchRankDetail(LocalDate date) { deleteZSetKey(searchRankDetailPrefix, date); }

    private void deleteZSetKey(String prefix, LocalDate date) {
        String redisKey = makeRedisKey(prefix, date);
        redisTemplate.delete(redisKey);
    }

    private String makeRedisKey(String prefix, LocalDate date) {
        String dateStr = date.format(DAILY_KEY_FORMATTER);
        return prefix + dateStr;
    }

}
