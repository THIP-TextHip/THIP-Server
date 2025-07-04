package konkuk.thip.book.adapter.out.persistence;

import konkuk.thip.book.application.port.out.BookRedisCommandPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BookCommandRedisAdapter implements BookRedisCommandPort {

    private final RedisTemplate<String, String> redisTemplate;
    private final DateTimeFormatter DAILY_KEY_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    @Value("${app.redis.search-count-prefix}")
    private String searchCountPrefix;

    @Value("${app.redis.search-rank-prefix}")
    private String searchRankPrefix;


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
    public void deleteBookSearchRank(LocalDate date) {
        deleteZSetKey(searchRankPrefix, date);
    }

    @Override
    public void deleteBookSearchCount(LocalDate date) {
        deleteZSetKey(searchCountPrefix, date);
    }

    private void deleteZSetKey(String prefix, LocalDate date) {
        String redisKey = makeRedisKey(prefix, date);
        redisTemplate.delete(redisKey);
    }

    private String makeRedisKey(String prefix, LocalDate date) {
        String dateStr = date.format(DAILY_KEY_FORMATTER);
        return prefix + dateStr;
    }


}
