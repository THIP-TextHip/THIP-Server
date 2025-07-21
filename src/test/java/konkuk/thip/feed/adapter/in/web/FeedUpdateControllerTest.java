package konkuk.thip.feed.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.feed.adapter.out.persistence.repository.FeedJpaRepository;
import konkuk.thip.feed.adapter.out.persistence.repository.Tag.TagJpaRepository;
import konkuk.thip.room.adapter.out.jpa.CategoryJpaEntity;
import konkuk.thip.room.adapter.out.persistence.repository.category.CategoryJpaRepository;
import konkuk.thip.user.adapter.out.jpa.AliasJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.adapter.out.persistence.repository.alias.AliasJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static konkuk.thip.common.exception.code.ErrorCode.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@DisplayName("[단위] 피드 수정 api controller 단위 테스트")
class FeedUpdateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;
    @Autowired private AliasJpaRepository aliasJpaRepository;
    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private CategoryJpaRepository categoryJpaRepository;
    @Autowired private BookJpaRepository bookJpaRepository;
    @Autowired private TagJpaRepository tagJpaRepository;
    @Autowired private FeedJpaRepository feedJpaRepository;

    private Long savedFeedId;
    private Long creatorUserId;

    @BeforeEach
    void setUp() {

        AliasJpaEntity alias = aliasJpaRepository.save(TestEntityFactory.createLiteratureAlias());
        UserJpaEntity user = userJpaRepository.save(TestEntityFactory.createUser(alias));
        CategoryJpaEntity category = categoryJpaRepository.save(TestEntityFactory.createLiteratureCategory(alias));

        tagJpaRepository.save(TestEntityFactory.createTag(category, "소설추천"));
        tagJpaRepository.save(TestEntityFactory.createTag(category, "책추천"));
        tagJpaRepository.save(TestEntityFactory.createTag(category, "오늘의책"));
        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBookWithISBN("9788954682152"));
        savedFeedId = feedJpaRepository.save(TestEntityFactory.createFeed(user,book, true)).getPostId();
        creatorUserId = user.getUserId();
    }

    private Map<String, Object> buildValidUpdateRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("contentBody", "수정된 테스트 콘텐츠");
        request.put("isPublic", true);
        request.put("tagList", List.of("책추천", "소설추천"));
        request.put("remainImageUrls", List.of());
        return request;
    }

    private void assertBadRequest(int expectedCode, Map<String, Object> request, String message) throws Exception {
        mockMvc.perform(patch("/feeds/1")
                                .requestAttr("userId", 100L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(expectedCode))
                .andExpect(jsonPath("$.message", containsString(message)));
    }

    @Nested
    @DisplayName("피드 관련 검증")
    class BasicValidation {

        @Test
        @DisplayName("존재하지 않는 피드를 수정하려는 경우 404 반환")
        void updateNonExistentFeed() throws Exception {
            Map<String, Object> req = buildValidUpdateRequest();
            mockMvc.perform(patch("/feeds/99999")
                            .requestAttr("userId", 100L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(req))
                    )
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(FEED_NOT_FOUND.getCode()))
                    .andExpect(jsonPath("$.message", containsString("존재하지 않는 FEED 입니다.")));
        }

        @Test
        @DisplayName("피드 생성자가 아닌 유저가 수정하려는 경우 400 반환")
        void unauthorizedFeedEdit() throws Exception {
            Map<String, Object> req = buildValidUpdateRequest();
            mockMvc.perform(patch("/feeds/" + savedFeedId)
                    .requestAttr("userId",100L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(req))
                    )
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(FEED_UPDATE_FORBIDDEN.getCode()))
                    .andExpect(jsonPath("$.message", containsString("피드 수정 권한이 없습니다.")));
        }

    }

    @Nested
    @DisplayName("태그 검증")
    class TagValidation {

        @Test
        @DisplayName("태그리스트 중 존재하지 않는 태그가 있을 때 400 반환")
        void invalidTagNames() throws Exception {
            Map<String, Object> req = buildValidUpdateRequest();
            req.put("tagList", List.of("에세이", "휴식", "힐링"));
            mockMvc.perform(patch("/feeds/" + savedFeedId)
                            .requestAttr("userId",creatorUserId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(req))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(TAG_NAME_NOT_MATCH.getCode()))
                    .andExpect(jsonPath("$.message", containsString("일치하는 태그 이름이 없습니다")))
                    .andExpect(jsonPath("$.message", containsString("에세이")))
                    .andExpect(jsonPath("$.message", containsString("휴식")))
                    .andExpect(jsonPath("$.message", containsString("힐링")));
        }

        @Test
        @DisplayName("태그가 5개 초과일 경우 400 반환")
        void tooManyTags() throws Exception {
            Map<String, Object> req = buildValidUpdateRequest();
            req.put("tagList", List.of("t1","t2","t3","t4","t5","t6"));
            assertBadRequest(INVALID_FEED_COMMAND.getCode(), req, "태그는 최대 5개까지 입력할 수 있습니다.");
        }

        @Test
        @DisplayName("태그가 중복되어 있을 경우 400 반환")
        void duplicatedTags() throws Exception {
            Map<String, Object> req = buildValidUpdateRequest();
            req.put("tagList", List.of("중복", "중복"));
            assertBadRequest(INVALID_FEED_COMMAND.getCode(), req, "태그는 중복 될 수 없습니다.");
        }

    }

    @Nested
    @DisplayName("이미지 검증")
    class ImageValidation {

        @Test
        @DisplayName("이미지가 3개 초과되면 400 반환")
        void tooManyImages() throws Exception {
            Map<String, Object> req = buildValidUpdateRequest();
            req.put("remainImageUrls",
                    List.of("https://s3.../profile1.png", "https://s3.../profile2.png",
                    "https://s3.../profile3.png", "https://s3.../profile4.png"));
            mockMvc.perform(patch("/feeds/" + savedFeedId)
                            .requestAttr("userId",creatorUserId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(req))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(INVALID_FEED_COMMAND.getCode()))
                    .andExpect(jsonPath("$.message",containsString("이미지는 최대 3개까지 업로드할 수 있습니다.")));
        }

        @Test
        @DisplayName("이미지 url이 잘못되었을 때 400 반환")
        void invalidImageUrl() throws Exception {
            Map<String, Object> req = buildValidUpdateRequest();
            req.put("remainImageUrls", List.of("https://s3.../profile1.png"));
            mockMvc.perform(patch("/feeds/" + savedFeedId)
                            .requestAttr("userId",creatorUserId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(req))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(INVALID_FEED_COMMAND.getCode()))
                    .andExpect(jsonPath("$.message", containsString("해당 이미지는 이 피드에 존재하지 않습니다")));
        }
    }

}
