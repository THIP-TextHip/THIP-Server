package konkuk.thip.feed.adapter.in.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.feed.adapter.out.jpa.FeedJpaEntity;
import konkuk.thip.feed.adapter.out.persistence.repository.FeedJpaRepository;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.domain.value.Alias;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static konkuk.thip.feed.domain.value.Tag.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@DisplayName("[통합] 피드 생성 api 통합 테스트")
class FeedCreateApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private BookJpaRepository bookJpaRepository;

    @Autowired
    private FeedJpaRepository feedJpaRepository;

    private Alias alias;
    private UserJpaEntity user;

    @BeforeEach
    void setUp() {
        alias = TestEntityFactory.createLiteratureAlias();
        user = userJpaRepository.save(TestEntityFactory.createUser(alias));
    }

    @Test
    @DisplayName("isbn 에 해당하는 책이 DB에 존재할 때, 해당 책과 연관된 피드를 생성할 수 있다.")
    void createFeedWithBookExistsInDB() throws Exception {

        // given
        bookJpaRepository.save(TestEntityFactory.createBookWithISBN("9788954682152"));

        Map<String, Object> request = new HashMap<>();
        request.put("isbn", "9788954682152"); // 책 ISBN
        request.put("contentBody", "이 책 정말 좋아요.");
        request.put("isPublic", true);
        request.put("tagList", List.of(KOREAN_NOVEL.getValue(), FOREIGN_NOVEL.getValue(), CLASSIC_LITERATURE.getValue())); //실제 태그 값
        request.put("imageUrls", List.of(
                "https://mock-s3-bucket/fake-image-url1.jpg",
                "https://mock-s3-bucket/fake-image-url2.jpg"
        ));

        // when
        ResultActions result = mockMvc.perform(post("/feeds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .requestAttr("userId", user.getUserId())
                       .content(objectMapper.writeValueAsString(request)));


        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.feedId").exists());

        String json = result.andReturn().getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(json);
        Long postId = root.path("data").path("feedId").asLong();

        // DB에 피드가 저장되었는지 확인
        FeedJpaEntity feedJpaEntity = feedJpaRepository.findById(postId).orElse(null);

        assertThat(feedJpaEntity).isNotNull();
        assertThat(feedJpaEntity.getBookJpaEntity().getIsbn()).isEqualTo("9788954682152");
        assertThat(feedJpaEntity.getUserJpaEntity().getUserId()).isEqualTo(user.getUserId());
        assertThat(feedJpaEntity.getIsPublic()).isTrue();
        assertThat(feedJpaEntity.getPostId()).isEqualTo(postId);
    }

    @Test
    @DisplayName("isbn 에 해당하는 책이 DB에 존재하지 않을 경우, 외부 api를 통해 책을 DB에 저장한 후 연관된 피드를 생성할 수 있다.")
    void createFeedWithBookNotExists_usesExternalAPI() throws Exception {

        // given
        String isbn = "9791168342941"; // 외부 API에서 정상 조회되는 실제 ISBN (DB에는 저장되어 있지 않음)
        Map<String, Object> request = new HashMap<>();
        request.put("isbn", isbn);
        request.put("contentBody", "외부 API를 통해 등록된 책 피드입니다.");
        request.put("isPublic", true);
        request.put("tagList", List.of(KOREAN_NOVEL.getValue(), FOREIGN_NOVEL.getValue(), CLASSIC_LITERATURE.getValue())); //실제 태그 값

        // when
        ResultActions result = mockMvc.perform(post("/feeds")
                .contentType(MediaType.APPLICATION_JSON)
                .requestAttr("userId", user.getUserId())
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.feedId").exists());

        String json = result.andReturn().getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(json);
        Long postId = root.path("data").path("feedId").asLong();

        // DB에 피드가 저장되었는지 확인
        FeedJpaEntity feedJpaEntity = feedJpaRepository.findById(postId).orElse(null);
        assertThat(feedJpaEntity).isNotNull();
        assertThat(feedJpaEntity.getBookJpaEntity().getIsbn()).isEqualTo(isbn);
        assertThat(feedJpaEntity.getUserJpaEntity().getUserId()).isEqualTo(user.getUserId());
        assertThat(feedJpaEntity.getIsPublic()).isTrue();
        assertThat(feedJpaEntity.getPostId()).isEqualTo(postId);

        // 책이 실제로 DB에 저장되었는지 확인
        BookJpaEntity savedBook = bookJpaRepository.findAll().stream()
                .filter(book -> isbn.equals(book.getIsbn()))
                .findFirst()
                .orElse(null);

        assertThat(savedBook).isNotNull();
        assertThat(savedBook.getIsbn()).isEqualTo(isbn);
    }

    @Test
    @DisplayName("피드 생성시, 이미지 파일이 여러 개 들어올 경우, 피드에 대응되는 Content가 각각 생성된 후 관된 피드를 생성할 수 있다.")
    void createFeedWithImages_createsContentEntities() throws Exception {

        // given
        bookJpaRepository.save(TestEntityFactory.createBookWithISBN("9788954682152"));

        Map<String, Object> request = new HashMap<>();
        request.put("isbn", "9788954682152"); // 책 ISBN
        request.put("contentBody", "이미지 테스트 피드");
        request.put("isPublic", true);
        request.put("tagList", List.of(KOREAN_NOVEL.getValue())); //실제 태그 값
        request.put("imageUrls", List.of(
                "https://mock-s3-bucket/fake-image-url1.jpg",
                "https://mock-s3-bucket/fake-image-url2.jpg"
        ));

        // when
        ResultActions result = mockMvc.perform(post("/feeds")
                .contentType(MediaType.APPLICATION_JSON)
                .requestAttr("userId", user.getUserId())
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.feedId").exists());

        String json = result.andReturn().getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(json);
        Long postId = root.path("data").path("feedId").asLong();

        // DB에 피드가 저장되었는지 확인
        FeedJpaEntity feedJpaEntity = feedJpaRepository.findById(postId).orElse(null);
        assertThat(feedJpaEntity).isNotNull();
        assertThat(feedJpaEntity.getBookJpaEntity().getIsbn()).isEqualTo("9788954682152");
        assertThat(feedJpaEntity.getUserJpaEntity().getUserId()).isEqualTo(user.getUserId());
        assertThat(feedJpaEntity.getIsPublic()).isTrue();
        assertThat(feedJpaEntity.getPostId()).isEqualTo(postId);

        // Content 검증
        assertThat(feedJpaEntity.getContentList()).hasSize(2);
        assertThat(feedJpaEntity.getContentList())
                .containsExactlyInAnyOrder(
                        "https://mock-s3-bucket/fake-image-url1.jpg",
                        "https://mock-s3-bucket/fake-image-url2.jpg"
                );

    }

    @Test
    @DisplayName("이미지가 없는 피드를 생성하면 Feed의 contentList는 비어 있어야 한다.")
    void createFeedWithoutImages_shouldHaveEmptyContentList() throws Exception {
        // given
        bookJpaRepository.save(TestEntityFactory.createBookWithISBN("9788954682152"));

        Map<String, Object> request = new HashMap<>();
        request.put("isbn", "9788954682152");
        request.put("contentBody", "이미지 없는 피드");
        request.put("isPublic", true);
        request.put("tagList", List.of(KOREAN_NOVEL.getValue())); //실제 태그 값

        // when
        ResultActions result = mockMvc.perform(post("/feeds")
                .contentType(MediaType.APPLICATION_JSON)
                .requestAttr("userId", user.getUserId())
                .content(objectMapper.writeValueAsString(request)));


        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.feedId").exists());

        String json = result.andReturn().getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(json);
        Long postId = root.path("data").path("feedId").asLong();

        FeedJpaEntity feed = feedJpaRepository.findById(postId).orElseThrow();
        assertThat(feed.getContentList()).isEmpty();
    }


    @Test
    @DisplayName("피드 생성시 태그가 들어오면, 태그를 포함한 피드가 DB에 저장된다.")
    void createFeedWithTags_createsFeedTagMappings() throws Exception {

        // given
        bookJpaRepository.save(TestEntityFactory.createBookWithISBN("9788954682152"));

        Map<String, Object> request = new HashMap<>();
        request.put("isbn", "9788954682152");
        request.put("contentBody", "태그 매핑 테스트 중입니다.");
        request.put("isPublic", true);
        request.put("tagList", List.of(KOREAN_NOVEL.getValue(), FOREIGN_NOVEL.getValue(), CLASSIC_LITERATURE.getValue())); //실제 태그 값

        // when
        ResultActions result = mockMvc.perform(post("/feeds")
                .contentType(MediaType.APPLICATION_JSON)
                .requestAttr("userId", user.getUserId())
                .content(objectMapper.writeValueAsString(request)));


        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.feedId").exists());

        String json = result.andReturn().getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(json);
        Long postId = root.path("data").path("feedId").asLong();


        // DB에 피드가 저장되었는지 확인
        FeedJpaEntity feedJpaEntity = feedJpaRepository.findById(postId).orElse(null);
        assertThat(feedJpaEntity).isNotNull();
        assertThat(feedJpaEntity.getBookJpaEntity().getIsbn()).isEqualTo("9788954682152");
        assertThat(feedJpaEntity.getUserJpaEntity().getUserId()).isEqualTo(user.getUserId());
        assertThat(feedJpaEntity.getIsPublic()).isTrue();
        assertThat(feedJpaEntity.getPostId()).isEqualTo(postId);

        // 저장된 피드가 3개의 태그를 가지는지 확인
        assertThat(feedJpaEntity.getTagList().toUnmodifiableList().size()).isEqualTo(3);
    }

    @Test
    @DisplayName("태그가 없는 피드는 태그가 없는 채로 DB에 저장된다.")
    void createFeedWithoutTags_shouldNotHaveFeedTags() throws Exception {
        // given
        bookJpaRepository.save(TestEntityFactory.createBookWithISBN("9788954682152"));

        Map<String, Object> request = new HashMap<>();
        request.put("isbn", "9788954682152");
        request.put("contentBody", "태그 없는 피드");
        request.put("isPublic", true);
        request.put("tagList", List.of());  // 태그 없음

        // when
        ResultActions result = mockMvc.perform(post("/feeds")
                .contentType(MediaType.APPLICATION_JSON)
                .requestAttr("userId", user.getUserId())
                .content(objectMapper.writeValueAsString(request)));


        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.feedId").exists());

        String json = result.andReturn().getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(json);
        Long postId = root.path("data").path("feedId").asLong();

        FeedJpaEntity feedJpaEntity = feedJpaRepository.findById(postId).orElse(null);
        assertThat(feedJpaEntity.getTagList().toUnmodifiableList().size()).isEqualTo(0);
    }
}
