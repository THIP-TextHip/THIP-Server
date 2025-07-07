package konkuk.thip.book.adapter.in.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import konkuk.thip.book.application.port.in.dto.BookMostSearchResult;
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


    private LocalDate yesterday;
    private String rankKey;


    private final DateTimeFormatter DAILY_KEY_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Value("${app.redis.search-rank-detail-prefix}")
    private String searchRankDetailPrefix;

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
    @DisplayName("어제 랭킹 Top 5를 정상적으로 조회한다")
    void getMostSearchedBooks_returnsRankList() throws Exception {
        // given: 어제 날짜의 랭킹 상세정보(JSON)를 Redis에 저장
        yesterday = LocalDate.now().minusDays(1);
        String detailKey = searchRankDetailPrefix + yesterday.format(DAILY_KEY_FORMATTER);

        List<BookMostSearchResult.BookRankInfo> bookRankInfos = List.of(
                BookMostSearchResult.BookRankInfo.builder()
                        .rank(1)
                        .title("책1")
                        .imageUrl("http://image1.jpg")
                        .isbn("9788954682152")
                        .build(),
                BookMostSearchResult.BookRankInfo.builder()
                        .rank(2)
                        .title("책2")
                        .imageUrl("http://image2.jpg")
                        .isbn("9788991742178")
                        .build(),
                BookMostSearchResult.BookRankInfo.builder()
                        .rank(3)
                        .title("책3")
                        .imageUrl("http://image3.jpg")
                        .isbn("9791198783400")
                        .build()
        );
        String json = objectMapper.writeValueAsString(bookRankInfos);
        redisTemplate.opsForValue().set(detailKey, json);

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

        String responseJson = result.andReturn().getResponse().getContentAsString();
        JsonNode bookList = objectMapper.readTree(responseJson).path("data").path("bookList");

        assertThat(bookList).isNotNull();
        assertThat(bookList.size()).isEqualTo(3);

        // 순서 및 필드 검증
        assertThat(bookList.get(0).path("isbn").asText()).isEqualTo("9788954682152");
        assertThat(bookList.get(1).path("isbn").asText()).isEqualTo("9788991742178");
        assertThat(bookList.get(2).path("isbn").asText()).isEqualTo("9791198783400");
    }

    @Test
    @DisplayName("랭킹 데이터가 없으면 빈 리스트를 반환한다")
    void getMostSearchedBooks_returnsEmptyList_whenNoData() throws Exception {
        // given: 어제 날짜의 랭킹 상세정보 키를 삭제
        yesterday = LocalDate.now().minusDays(1);
        String detailKey = searchRankDetailPrefix + yesterday.format(DAILY_KEY_FORMATTER);
        redisTemplate.delete(detailKey);

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

        String responseJson = result.andReturn().getResponse().getContentAsString();
        JsonNode bookList = objectMapper.readTree(responseJson).path("data").path("bookList");

        assertThat(bookList).isNotNull();
        assertThat(bookList.size()).isEqualTo(0);
    }


}
