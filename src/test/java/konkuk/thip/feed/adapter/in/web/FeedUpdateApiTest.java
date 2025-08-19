package konkuk.thip.feed.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.config.TestS3MockConfig;
import konkuk.thip.feed.adapter.out.jpa.ContentJpaEntity;
import konkuk.thip.feed.adapter.out.jpa.FeedJpaEntity;
import konkuk.thip.feed.adapter.out.persistence.repository.Content.ContentJpaRepository;
import konkuk.thip.feed.adapter.out.persistence.repository.FeedJpaRepository;
import konkuk.thip.feed.adapter.out.persistence.repository.FeedTag.FeedTagJpaRepository;
import konkuk.thip.feed.adapter.out.persistence.repository.Tag.TagJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.category.CategoryJpaRepository;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.adapter.out.persistence.repository.alias.AliasJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static konkuk.thip.feed.domain.Tag.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@Import(TestS3MockConfig.class)
@DisplayName("[통합] 피드 수정 api 통합 테스트")
class FeedUpdateApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;
    @Autowired private AliasJpaRepository aliasJpaRepository;
    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private CategoryJpaRepository categoryJpaRepository;
    @Autowired private BookJpaRepository bookJpaRepository;
    @Autowired private TagJpaRepository tagJpaRepository;
    @Autowired private FeedJpaRepository feedJpaRepository;
    @Autowired private FeedTagJpaRepository feedTagJpaRepository;
    @Autowired private ContentJpaRepository contentJpaRepository;

    private UserJpaEntity user;
    private BookJpaEntity book;
    private FeedJpaEntity feed;
    private TagJpaEntity tag1;
    private TagJpaEntity tag2;
    private TagJpaEntity tag3;

    @BeforeEach
    void setUp() {
        AliasJpaEntity alias = aliasJpaRepository.save(TestEntityFactory.createLiteratureAlias());
        user = userJpaRepository.save(TestEntityFactory.createUser(alias));
        CategoryJpaEntity category = categoryJpaRepository.save(TestEntityFactory.createLiteratureCategory(alias));

        tag1 = tagJpaRepository.save(TestEntityFactory.createLiteratureTag(category,KOREAN_NOVEL.getValue()));
        tag2 = tagJpaRepository.save(TestEntityFactory.createLiteratureTag(category,FOREIGN_NOVEL.getValue()));
        tag3 = tagJpaRepository.save(TestEntityFactory.createLiteratureTag(category, CLASSIC_LITERATURE.getValue()));
        book = bookJpaRepository.save(TestEntityFactory.createBookWithISBN("9788954682152"));
        feed = feedJpaRepository.save(TestEntityFactory.createFeed(user,book, true));
    }

    @Test
    @DisplayName("여러 태그가 있는 피드에서 태그를 수정하면 최종 태그로 반영되어 저장된다.")
    void updateTaggedFeed_shouldUpdateTagsCorrectly() throws Exception {

        // given
        Long feedId = feed.getPostId();

        // 기존 태그 3개 연관
        List<TagJpaEntity> existingTags = List.of(tag1, tag2, tag3);
        List<FeedTagJpaEntity> mappings = existingTags.stream()
                .map(tag -> TestEntityFactory.createFeedTagMapping(feed, tag))
                .toList();

        feedTagJpaRepository.saveAll(mappings);

        // 수정 요청
        Map<String, Object> request = new HashMap<>();
        request.put("contentBody", "태그 갱신 테스트");
        request.put("isPublic", false);
        request.put("tagList", List.of(KOREAN_NOVEL.getValue(),FOREIGN_NOVEL.getValue())); // 하나 제거됨

        // when
        ResultActions result = mockMvc.perform(patch("/feeds/{feedId}", feedId)
                .requestAttr("userId", user.getUserId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isOk());
        long tagCount = feedTagJpaRepository.findAll().stream()
                .filter(ft -> ft.getFeedJpaEntity().getPostId().equals(feedId))
                .count();
        assertThat(tagCount).isEqualTo(2);
    }

    @Test
    @DisplayName("이미지가 여러 개인 피드에서 이미지 일부만 유지하도록 수정할 수 있다.")
    void updateImageFeed_shouldRetainSomeImagesOnly() throws Exception {

        // given
        List<String> originalImages = List.of(
                "https://s3-mock/image-1.jpg",
                "https://s3-mock/image-2.jpg",
                "https://s3-mock/image-3.jpg"
        );

        FeedJpaEntity feed = feedJpaRepository.save(TestEntityFactory.createFeedWithContents(user, book, originalImages, true));
        Long feedId = feed.getPostId();

        // 수정 요청: 이미지 1개만 유지
        Map<String, Object> request = new HashMap<>();
        request.put("contentBody", "이미지 삭제 테스트");
        request.put("isPublic", false);
        request.put("remainImageUrls", List.of("https://s3-mock/image-2.jpg")); // 나머지 삭제

        // when
        ResultActions result = mockMvc.perform(patch("/feeds/{feedId}", feedId)
                .requestAttr("userId", user.getUserId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isOk());
        FeedJpaEntity updatedFeed = feedJpaRepository.findById(feedId).orElseThrow();
        assertThat(updatedFeed.getContentList()).hasSize(1);
        assertThat(updatedFeed.getContentList().get(0).getContentUrl()).isEqualTo("https://s3-mock/image-2.jpg");
        List<ContentJpaEntity> contentRows = contentJpaRepository.findAll().stream()
                .filter(c -> c.getPostJpaEntity().getPostId().equals(feedId))
                .toList();
        assertThat(contentRows).hasSize(1);
        assertThat(contentRows.get(0).getContentUrl()).isEqualTo("https://s3-mock/image-2.jpg");
    }

    @Test
    @DisplayName("피드의 내용, 공개 여부, 태그, 이미지 전체를 모두 수정할 수 있다.")
    void updateFeedWithAllFields_shouldModifyEverythingCorrectly() throws Exception {

        // given
        // 기존 이미지 3개
        List<String> originalImages = List.of(
                "https://s3-mock/image-1.jpg",
                "https://s3-mock/image-2.jpg",
                "https://s3-mock/image-3.jpg"
        );
        FeedJpaEntity feed = feedJpaRepository.save(TestEntityFactory.createFeedWithContents(user, book, originalImages, true));
        Long feedId = feed.getPostId();

        // 기존 태그 3개 매핑
        List<TagJpaEntity> existingTags = List.of(tag1, tag2, tag3);
        List<FeedTagJpaEntity> tagMappings = existingTags.stream()
                .map(tag -> TestEntityFactory.createFeedTagMapping(feed, tag))
                .collect(Collectors.toList());
        feedTagJpaRepository.saveAll(tagMappings);

        // 수정 요청: 태그 일부 삭제 & 이미지 일부 삭제 & 본문 변경 & 공개 여부 변경
        Map<String, Object> request = new HashMap<>();
        request.put("contentBody", "전부 수정되는 피드 테스트");
        request.put("isPublic", false);
        request.put("remainImageUrls", List.of("https://s3-mock/image-2.jpg")); // 이미지 1개 유지
        request.put("tagList", List.of(KOREAN_NOVEL.getValue(),FOREIGN_NOVEL.getValue())); // 하나 제거됨

        // when
        ResultActions result = mockMvc.perform(patch("/feeds/{feedId}", feedId)
                .requestAttr("userId", user.getUserId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isOk());

        FeedJpaEntity updated = feedJpaRepository.findById(feedId).orElseThrow();
        // 1. 본문
        assertThat(updated.getContent()).isEqualTo("전부 수정되는 피드 테스트");
        // 2. 공개 여부
        assertThat(updated.getIsPublic()).isFalse();
        // 3. 이미지
        assertThat(updated.getContentList()).hasSize(1);
        assertThat(updated.getContentList().get(0).getContentUrl()).isEqualTo("https://s3-mock/image-2.jpg");
        List<ContentJpaEntity> contentRows = contentJpaRepository.findAll().stream()
                .filter(c -> c.getPostJpaEntity().getPostId().equals(feedId))
                .toList();
        assertThat(contentRows).hasSize(1);
        assertThat(contentRows.get(0).getContentUrl()).isEqualTo("https://s3-mock/image-2.jpg");
        // 4. 태그 갯수
        long tagCount = feedTagJpaRepository.findAll().stream()
                .filter(tag -> tag.getFeedJpaEntity().getPostId().equals(feedId))
                .count();
        assertThat(tagCount).isEqualTo(2);
    }

}
