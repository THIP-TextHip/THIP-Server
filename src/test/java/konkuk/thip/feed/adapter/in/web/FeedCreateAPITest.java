package konkuk.thip.feed.adapter.in.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.config.TestS3MockConfig;
import konkuk.thip.feed.adapter.out.jpa.FeedJpaEntity;
import konkuk.thip.feed.adapter.out.persistence.repository.FeedJpaRepository;
import konkuk.thip.feed.adapter.out.persistence.repository.FeedTag.FeedTagJpaRepository;
import konkuk.thip.feed.adapter.out.persistence.repository.Tag.TagJpaRepository;
import konkuk.thip.room.adapter.out.jpa.CategoryJpaEntity;
import konkuk.thip.room.adapter.out.persistence.repository.category.CategoryJpaRepository;
import konkuk.thip.user.adapter.out.jpa.AliasJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.alias.AliasJpaRepository;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static konkuk.thip.feed.domain.Tag.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@Import(TestS3MockConfig.class)
@DisplayName("[통합] 피드 생성 api 통합 테스트")
class FeedCreateAPITest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AliasJpaRepository aliasJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

    @Autowired
    private BookJpaRepository bookJpaRepository;

    @Autowired
    private TagJpaRepository tagJpaRepository;

    @Autowired
    private FeedJpaRepository feedJpaRepository;

    @Autowired
    private FeedTagJpaRepository feedTagJpaRepository;

    private AliasJpaEntity alias;
    private UserJpaEntity user;
    private CategoryJpaEntity category;

    @BeforeEach
    void setUp() {
        alias = aliasJpaRepository.save(TestEntityFactory.createLiteratureAlias());
        user = userJpaRepository.save(TestEntityFactory.createUser(alias));
        category = categoryJpaRepository.save(TestEntityFactory.createLiteratureCategory(alias));
        tagJpaRepository.save(TestEntityFactory.createTag(category,KOREAN_NOVEL.getValue()));
        tagJpaRepository.save(TestEntityFactory.createTag(category,FOREIGN_NOVEL.getValue()));
        tagJpaRepository.save(TestEntityFactory.createTag(category,CLASSIC_LITERATURE.getValue()));

    }

    @AfterEach
    void tearDown() {
        feedTagJpaRepository.deleteAll();
        feedJpaRepository.deleteAll();
        bookJpaRepository.deleteAll();
        tagJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
        categoryJpaRepository.deleteAll();
        aliasJpaRepository.deleteAll();
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
        request.put("category", "문학"); //실제 카테고리 값
        request.put("tagList", List.of(KOREAN_NOVEL.getValue(), FOREIGN_NOVEL.getValue(), CLASSIC_LITERATURE.getValue())); //실제 태그 값

        MockMultipartFile requestPart = new MockMultipartFile(
                "request",                     // requestPart name
                "",                            // 빈 파일명
                MediaType.APPLICATION_JSON_VALUE,  // Content type
                objectMapper.writeValueAsBytes(request) // 우릴 JSON 바이트로
        );

        // when
        ResultActions result = mockMvc.perform(multipart("/feeds")
                        .file(requestPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .requestAttr("userId", user.getUserId()));

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
        request.put("category", "문학"); //실제 카테고리 값
        request.put("tagList", List.of(KOREAN_NOVEL.getValue(), FOREIGN_NOVEL.getValue(), CLASSIC_LITERATURE.getValue())); //실제 태그 값

        MockMultipartFile requestPart = new MockMultipartFile(
                "request",                     // requestPart name
                "",                            // 빈 파일명
                MediaType.APPLICATION_JSON_VALUE,  // Content type
                objectMapper.writeValueAsBytes(request) // 우릴 JSON 바이트로
        );

        // when
        ResultActions result = mockMvc.perform(multipart("/feeds")
                .file(requestPart)
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                .requestAttr("userId", user.getUserId()));

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
        request.put("category", "문학"); //실제 카테고리 값
        request.put("tagList", List.of(KOREAN_NOVEL.getValue())); //실제 태그 값

        MockMultipartFile requestPart = new MockMultipartFile(
                "request", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
        );

        MockMultipartFile image1 = new MockMultipartFile("images", "img1.png", "image/png", "data1".getBytes());
        MockMultipartFile image2 = new MockMultipartFile("images", "img2.jpg", "image/jpeg", "data2".getBytes());
        MockMultipartFile image3 = new MockMultipartFile("images", "img3.jpeg", "image/jpeg", "data3".getBytes());

        // when
        ResultActions result = mockMvc.perform(multipart("/feeds")
                .file(requestPart)
                .file(image1)
                .file(image2)
                .file(image3)
                .requestAttr("userId", user.getUserId())
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
        );

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
        assertThat(feedJpaEntity.getContentList()).hasSize(3);
        assertThat(feedJpaEntity.getContentList())
                .extracting("contentUrl")
                .containsExactlyInAnyOrder(
                        "https://mock-s3-bucket/fake-image-url.jpg",
                        "https://mock-s3-bucket/fake-image-url.jpg",
                        "https://mock-s3-bucket/fake-image-url.jpg"
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
        request.put("category", "문학");
        request.put("tagList", List.of(KOREAN_NOVEL.getValue())); //실제 태그 값

        MockMultipartFile requestPart = new MockMultipartFile(
                "request", "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
        );

        // when
        ResultActions result = mockMvc.perform(multipart("/feeds")
                .file(requestPart)
                .requestAttr("userId", user.getUserId())
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
        );

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
    @DisplayName("피드 생성시, 태그가 들어오면 feed_tags 매핑 테이블에 정상적으로 3개의 태그가 저장된된 후 관련 피드를 생성할 수 있다.")
    void createFeedWithTags_createsFeedTagMappings() throws Exception {

        // given
        bookJpaRepository.save(TestEntityFactory.createBookWithISBN("9788954682152"));

        Map<String, Object> request = new HashMap<>();
        request.put("isbn", "9788954682152");
        request.put("contentBody", "태그 매핑 테스트 중입니다.");
        request.put("isPublic", true);
        request.put("category", "문학");
        request.put("tagList", List.of(KOREAN_NOVEL.getValue(), FOREIGN_NOVEL.getValue(), CLASSIC_LITERATURE.getValue())); //실제 태그 값

        MockMultipartFile requestPart = new MockMultipartFile(
                "request", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
        );

        // when
        ResultActions result = mockMvc.perform(multipart("/feeds")
                .file(requestPart)
                .requestAttr("userId", user.getUserId())
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
        );

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

        // DB에 feed_tags 저장되었는지 확인
        long mappingCount = feedTagJpaRepository.findAll().stream()
                .filter(f -> f.getFeedJpaEntity().getPostId().equals(postId))
                .count();
        assertThat(mappingCount).isEqualTo(3);
    }

    @Test
    @DisplayName("카테고리와 태그가 없는 피드는 feed_tags 매핑이 없어야 한다.")
    void createFeedWithoutTags_shouldNotHaveFeedTags() throws Exception {
        // given
        bookJpaRepository.save(TestEntityFactory.createBookWithISBN("9788954682152"));

        Map<String, Object> request = new HashMap<>();
        request.put("isbn", "9788954682152");
        request.put("contentBody", "태그 없는 피드");
        request.put("isPublic", true);
        request.put("category", "");        // 카테고리 없이
        request.put("tagList", List.of());  // 태그 없음

        MockMultipartFile requestPart = new MockMultipartFile(
                "request", "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
        );

        // when
        ResultActions result = mockMvc.perform(multipart("/feeds")
                .file(requestPart)
                .requestAttr("userId", user.getUserId())
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.feedId").exists());

        String json = result.andReturn().getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(json);
        Long postId = root.path("data").path("feedId").asLong();

        long feedTagCount = feedTagJpaRepository.findAll().stream()
                .filter(f -> f.getFeedJpaEntity().getPostId().equals(postId))
                .count();

        assertThat(feedTagCount).isEqualTo(0);              // feed_tags 매핑 없음
    }


}
