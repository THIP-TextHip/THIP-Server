package konkuk.thip.feed.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.exception.code.ErrorCode;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.feed.adapter.in.web.request.FeedIsSavedRequest;
import konkuk.thip.feed.adapter.out.jpa.FeedJpaEntity;
import konkuk.thip.feed.adapter.out.persistence.repository.FeedJpaRepository;
import konkuk.thip.feed.adapter.out.persistence.repository.Tag.TagJpaRepository;
import konkuk.thip.room.adapter.out.jpa.CategoryJpaEntity;
import konkuk.thip.room.adapter.out.persistence.repository.category.CategoryJpaRepository;
import konkuk.thip.feed.adapter.out.jpa.SavedFeedJpaEntity;
import konkuk.thip.feed.adapter.out.persistence.repository.SavedFeedJpaRepository;
import konkuk.thip.user.adapter.out.jpa.AliasJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.adapter.out.persistence.repository.alias.AliasJpaRepository;
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

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[통합] 피드 저장 상태 변경 api 통합 테스트")
class FeedChangeSavedApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;
    @Autowired private AliasJpaRepository aliasJpaRepository;
    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private CategoryJpaRepository categoryJpaRepository;
    @Autowired private BookJpaRepository bookJpaRepository;
    @Autowired private TagJpaRepository tagJpaRepository;
    @Autowired private FeedJpaRepository feedJpaRepository;
    @Autowired private SavedFeedJpaRepository savedFeedJpaRepository;

    private UserJpaEntity user;
    private BookJpaEntity book;
    private FeedJpaEntity feed;

    @BeforeEach
    void setUp() {
        AliasJpaEntity alias = aliasJpaRepository.save(TestEntityFactory.createLiteratureAlias());
        user = userJpaRepository.save(TestEntityFactory.createUser(alias));
        CategoryJpaEntity category = categoryJpaRepository.save(TestEntityFactory.createLiteratureCategory(alias));

        tagJpaRepository.save(TestEntityFactory.createTag(category, "소설추천"));
        tagJpaRepository.save(TestEntityFactory.createTag(category, "책추천"));
        tagJpaRepository.save(TestEntityFactory.createTag(category, "오늘의책"));
        book = bookJpaRepository.save(TestEntityFactory.createBookWithISBN("9788954682152"));
        feed = feedJpaRepository.save(TestEntityFactory.createFeed(user,book, true));
    }

    @Test
    @DisplayName("피드를 처음 저장하면 [저장 성공]")
    void saveFeed_success() throws Exception {
        // given
        FeedIsSavedRequest request = new FeedIsSavedRequest(true);
        Long feedId = feed.getPostId();

        // when
        ResultActions result = mockMvc.perform(post("/feeds/{feedId}/saved", feedId)
                .requestAttr("userId", user.getUserId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.feedId").value(feedId))
                .andExpect(jsonPath("$.data.isSaved").value(true));

        // 실제 저장되었는지 검증
        List<SavedFeedJpaEntity> savedFeeds = savedFeedJpaRepository.findAllByUserId(user.getUserId());
        boolean exists = savedFeeds.stream()
                .anyMatch(entity -> entity.getFeedJpaEntity().getPostId().equals(feed.getPostId()));
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("이미 저장한 피드를 다시 저장하려하면 [400 에러 발생]")
    void saveFeed_alreadySaved_fail() throws Exception {
        // given
        savedFeedJpaRepository.save(TestEntityFactory.createSavedFeed(user, feed));
        FeedIsSavedRequest request = new FeedIsSavedRequest(true);

        // when
        ResultActions result = mockMvc.perform(post("/feeds/{feedId}/saved", feed.getPostId())
                .requestAttr("userId", user.getUserId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.FEED_ALREADY_SAVED.getCode()));
    }

    @Test
    @DisplayName("피드를 저장한 이후 삭제 요청하면 [피드 저장 삭제 성공]")
    void deleteFeed_success() throws Exception {
        // given
        savedFeedJpaRepository.save(TestEntityFactory.createSavedFeed(user, feed));
        FeedIsSavedRequest request = new FeedIsSavedRequest(false);

        // when
        ResultActions result = mockMvc.perform(post("/feeds/{feedId}/saved", feed.getPostId())
                .requestAttr("userId", user.getUserId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.feedId").value(feed.getPostId()))
                .andExpect(jsonPath("$.data.isSaved").value(false));

        List<SavedFeedJpaEntity> savedFeeds = savedFeedJpaRepository.findAllByUserId(user.getUserId());
        boolean exists = savedFeeds.stream()
                .anyMatch(entity -> entity.getFeedJpaEntity().getPostId().equals(feed.getPostId()));
        assertThat(exists).isFalse();

    }

    @Test
    @DisplayName("저장하지 않은 피드를 삭제하려고 하면 [400 에러 발생]")
    void deleteFeed_notSaved_fail() throws Exception {
        // given
        FeedIsSavedRequest request = new FeedIsSavedRequest(false);

        // when
        ResultActions result = mockMvc.perform(post("/feeds/{feedId}/saved", feed.getPostId())
                .requestAttr("userId", user.getUserId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.FEED_NOT_SAVED_CANNOT_DELETE.getCode()));
    }

    @Test
    @DisplayName("존재하지 않는 피드를 저장/삭제 요청시 [404 에러 발생]")
    void changeFeedSaveStatus_whenFeedNotExist_thenFail() throws Exception {

        // given
        Long invalidFeedId = 99999L;
        FeedIsSavedRequest request = new FeedIsSavedRequest(true);

        // when
        ResultActions result = mockMvc.perform(post("/feeds/{feedId}/saved", invalidFeedId)
                .requestAttr("userId", user.getUserId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.FEED_NOT_FOUND.getCode()));
    }

}
