package konkuk.thip.book.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import konkuk.thip.book.application.port.out.BookRedisCommandPort;
import konkuk.thip.book.application.port.out.BookRedisQueryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class BookSearchRankServiceTest {

    @Autowired
    private BookRedisQueryPort bookRedisQueryPort;

    @Autowired
    private BookRedisCommandPort bookRedisCommandPort;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private final DateTimeFormatter DAILY_KEY_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private LocalDate yesterday;

    @Value("${app.redis.search-count-prefix}")
    private String searchCountPrefix;


    @BeforeEach
    void setup() {

        // given: 어제 날짜의 카운트 키에 테스트용 데이터 3개를 저장
        yesterday = LocalDate.now().minusDays(1);
        String countKey = searchCountPrefix + yesterday.format(DAILY_KEY_FORMATTER);

        redisTemplate.opsForZSet().add(countKey, "9788954682152", 10);
        redisTemplate.opsForZSet().add(countKey, "9788991742178", 5);
        redisTemplate.opsForZSet().add(countKey, "9791198783400", 7);

    }

    @Test
    @DisplayName("어제의 검색 카운트 Top 5를 집계하여 랭킹 키에 저장한다")
    void updateDailySearchRank_shouldAggregateAndSaveTop5() {

        // given 어제 날짜의 카운트 키에 데이터가 저장되어 있음
        List<Map.Entry<String, Double>> top5 = bookRedisQueryPort.getBookSearchCountTopN(yesterday, 5);

        // when 기존 랭킹/카운트 키를 삭제하고, Top 5를 랭킹 키에 저장
        bookRedisCommandPort.deleteBookSearchRank(yesterday);
        bookRedisCommandPort.deleteBookSearchCount(yesterday);

        if (!top5.isEmpty()) {
            List<String> isbns = top5.stream().map(Map.Entry::getKey).toList();
            List<Double> scores = top5.stream().map(Map.Entry::getValue).toList();
            bookRedisCommandPort.saveBookSearchRank(isbns, scores, yesterday);
        }

        // then 랭킹 키에 Top 5가 내림차순으로 저장되어 있어야 함
        List<Map.Entry<String, Double>> savedTop5 = bookRedisQueryPort.getBookSearchRank(yesterday, 5);

        assertThat(savedTop5).isNotEmpty();
        assertThat(savedTop5.size()).isLessThanOrEqualTo(5);

        // 점수 내림차순 확인
        double previousScore = Double.MAX_VALUE;
        for (Map.Entry<String, Double> entry : savedTop5) {
            assertThat(entry.getValue()).isLessThanOrEqualTo(previousScore);
            previousScore = entry.getValue();
        }

        // 테스트용으로 저장된 isbn 포함 여부 확인
        assertThat(savedTop5.stream().map(Map.Entry::getKey))
                .containsAnyOf("9788954682152", "9788991742178", "9791198783400");
    }
}
