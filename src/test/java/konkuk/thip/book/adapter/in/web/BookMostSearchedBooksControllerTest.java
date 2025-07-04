package konkuk.thip.book.adapter.in.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import konkuk.thip.user.adapter.out.jpa.AliasJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserRole;
import konkuk.thip.user.adapter.out.persistence.AliasJpaRepository;
import konkuk.thip.user.adapter.out.persistence.UserJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class BookMostSearchedBooksControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private AliasJpaRepository aliasJpaRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;


    private LocalDate today;
    private String rankKey;


    private final DateTimeFormatter DAILY_KEY_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Value("${app.redis.search-rank-prefix}")
    private String searchRankPrefix;

    @BeforeEach
    void setUp() {

        AliasJpaEntity alias = aliasJpaRepository.save(AliasJpaEntity.builder()
                .value("책벌레")
                .color("blue")
                .imageUrl("http://image.url")
                .build());

        UserJpaEntity user = userJpaRepository.save(UserJpaEntity.builder()
                .oauth2Id("kakao_432708231")
                .nickname("User1")
                .imageUrl("https://avatar1.jpg")
                .role(UserRole.USER)
                .aliasForUserJpaEntity(alias)
                .build());

    }

    @AfterEach
    void tearDown() {
        userJpaRepository.deleteAll();
        aliasJpaRepository.deleteAll();
    }


    @Test
    @DisplayName("오늘 랭킹 Top 5를 정상적으로 조회한다")
    void getMostSearchedBooks_returnsRankList() throws Exception {

        // given 오늘 날짜의 랭킹 키에 테스트용 데이터 3개를 저장.
        today = LocalDate.now();
        rankKey = searchRankPrefix + today.format(DAILY_KEY_FORMATTER);

        redisTemplate.opsForZSet().add(rankKey, "9788954682152", 10);
        redisTemplate.opsForZSet().add(rankKey, "9788991742178", 5);
        redisTemplate.opsForZSet().add(rankKey, "9791198783400", 7);

        Long userId = userJpaRepository.findAll().get(0).getUserId();

        // when
        ResultActions result = mockMvc.perform(get("/books/most-searched")
                .requestAttr("userId", userId)
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bookList").isArray())
                .andExpect(jsonPath("$.data.bookList.length()").value(3));


        String json = result.andReturn().getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(json);
        JsonNode rankArray = jsonNode.path("data").path("bookList");

        assertThat(rankArray).isNotNull();
        assertThat(rankArray.size()).isEqualTo(3);


        // 점수 내림차순 확인 (Redis에서 직접 점수 확인)
        double previousScore = Double.MAX_VALUE;
        for (JsonNode bookRankInfo : rankArray) {
            String isbn = bookRankInfo.path("isbn").asText();
            Double score = redisTemplate.opsForZSet().score(rankKey, isbn);
            assertThat(score).isLessThanOrEqualTo(previousScore);
            previousScore = score;
        }

        // 저장된 isbn이 포함되어 있는지 확인
        List<String> isbns = List.of("9788954682152", "9788991742178", "9791198783400");
        for (JsonNode bookRankInfo : rankArray) {
            assertThat(isbns.contains(bookRankInfo.path("isbn").asText())).isTrue();
        }
    }

    @Test
    @DisplayName("랭킹 데이터가 없으면 빈 리스트를 반환한다")
    void getMostSearchedBooks_returnsEmptyList_whenNoData() throws Exception {

        // given 오늘 날짜의 랭킹 키를 삭제해서 데이터가 없는 상태로 만든다
        today = LocalDate.now();
        rankKey = searchRankPrefix + today.format(DAILY_KEY_FORMATTER);
        redisTemplate.delete(rankKey);

        Long userId = userJpaRepository.findAll().get(0).getUserId();

        // when
        ResultActions result = mockMvc.perform(get("/books/most-searched")
                .requestAttr("userId", userId)
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bookList").isArray())
                .andExpect(jsonPath("$.data.bookList.length()").value(0));

        String json = result.andReturn().getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(json);
        JsonNode bookList = jsonNode.path("data").path("bookList");

        assertThat(bookList).isNotNull();
        assertThat(bookList.size()).isEqualTo(0);
    }

}
