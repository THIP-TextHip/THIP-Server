package konkuk.thip.feed.adapter.in.web;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.feed.adapter.out.jpa.FeedJpaEntity;
import konkuk.thip.feed.adapter.out.jpa.TagJpaEntity;
import konkuk.thip.feed.adapter.out.persistence.repository.Content.ContentJpaRepository;
import konkuk.thip.feed.adapter.out.persistence.repository.FeedJpaRepository;
import konkuk.thip.feed.adapter.out.persistence.repository.FeedTag.FeedTagJpaRepository;
import konkuk.thip.feed.adapter.out.persistence.repository.Tag.TagJpaRepository;
import konkuk.thip.feed.domain.Tag;
import konkuk.thip.post.adapter.out.persistence.PostLikeJpaRepository;
import konkuk.thip.room.adapter.out.jpa.CategoryJpaEntity;
import konkuk.thip.room.adapter.out.persistence.repository.category.CategoryJpaRepository;
import konkuk.thip.feed.adapter.out.jpa.SavedFeedJpaEntity;
import konkuk.thip.feed.adapter.out.persistence.repository.SavedFeedJpaRepository;
import konkuk.thip.user.adapter.out.jpa.AliasJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.adapter.out.persistence.repository.alias.AliasJpaRepository;
import konkuk.thip.user.adapter.out.persistence.repository.following.FollowingJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static konkuk.thip.common.exception.code.ErrorCode.FEED_CAN_NOT_SHOW_PRIVATE_ONE;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[통합] 단일 피드 조회 api 통합 테스트")
class FeedShowSingleApiTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired private AliasJpaRepository aliasJpaRepository;
    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private CategoryJpaRepository categoryJpaRepository;
    @Autowired private FeedJpaRepository feedJpaRepository;
    @Autowired private TagJpaRepository tagJpaRepository;
    @Autowired private FeedTagJpaRepository feedTagJpaRepository;
    @Autowired private FollowingJpaRepository followingJpaRepository;
    @Autowired private BookJpaRepository bookJpaRepository;
    @Autowired private SavedFeedJpaRepository savedFeedJpaRepository;
    @Autowired private PostLikeJpaRepository postLikeJpaRepository;
    @Autowired private ContentJpaRepository contentJpaRepository;

    @AfterEach
    void tearDown() {
        postLikeJpaRepository.deleteAllInBatch();
        savedFeedJpaRepository.deleteAllInBatch();
        contentJpaRepository.deleteAllInBatch();
        feedTagJpaRepository.deleteAllInBatch();
        tagJpaRepository.deleteAllInBatch();
        categoryJpaRepository.deleteAllInBatch();
        feedJpaRepository.deleteAllInBatch();
        followingJpaRepository.deleteAllInBatch();
        userJpaRepository.deleteAllInBatch();
        aliasJpaRepository.deleteAllInBatch();
        bookJpaRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("단일 피드 조회를 요청할 경우, [피드 정보, 피드 작성자 정보, 피드와 연관된 태그들] 등의 정보를 반환한다.")
    void feed_show_single_test() throws Exception {
        //given
        AliasJpaEntity a0 = aliasJpaRepository.save(TestEntityFactory.createScienceAlias());
        UserJpaEntity me = userJpaRepository.save(TestEntityFactory.createUser(a0, "me"));
        UserJpaEntity feedCreator = userJpaRepository.save(TestEntityFactory.createUser(a0, "feedCreator"));

        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook());        // 공통 Book

        // 피드 및 피드 태그 생성
        CategoryJpaEntity c1 = categoryJpaRepository.save(TestEntityFactory.createScienceCategory(a0));
        TagJpaEntity t1 = tagJpaRepository.save(TestEntityFactory.createTag(c1, Tag.BOOK_REPORT.getValue()));
        TagJpaEntity t2 = tagJpaRepository.save(TestEntityFactory.createTag(c1, Tag.BOOK_RECOMMEND.getValue()));
        FeedJpaEntity f1 = feedJpaRepository.save(TestEntityFactory.createFeed(feedCreator, book, true, 50, 10, List.of("content1", "content2")));      // feedCreator가 작성한 공개 피드
        feedTagJpaRepository.save(TestEntityFactory.createFeedTagMapping(f1, t1));
        feedTagJpaRepository.save(TestEntityFactory.createFeedTagMapping(f1, t2));

        savedFeedJpaRepository.save(
                SavedFeedJpaEntity.builder()
                        .userJpaEntity(me)      // me가 f1을 저장하였음
                        .feedJpaEntity(f1)
                        .build()
        );

        //when //then
        mockMvc.perform(get("/feeds/{feedId}", f1.getPostId())
                        .requestAttr("userId", me.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.feedId", is(f1.getPostId().intValue())))
                .andExpect(jsonPath("$.data.creatorId", is(feedCreator.getUserId().intValue())))
                .andExpect(jsonPath("$.data.isbn", is(book.getIsbn())))
                .andExpect(jsonPath("$.data.contentUrls", is(List.of("content1", "content2"))))     // f1의 첨부파일 이미지 2개
                .andExpect(jsonPath("$.data.isSaved", is(true)))        // me가 f1을 저장했음
                .andExpect(jsonPath("$.data.isLiked", is(false)))       // me가 f1을 좋아하지 않음
                .andExpect(jsonPath("$.data.tagList", is(List.of(t1.getValue(), t2.getValue()))));      // f1의 태그 value 2개

    }

    @Test
    @DisplayName("피드 작성자가 아닌 다른 유저가 비공개 피드 단일 조회 요청을 할 경우, 400 error을 반환한다.")
    void feed_can_not_show_private_one() throws Exception {
        //given
        AliasJpaEntity a0 = aliasJpaRepository.save(TestEntityFactory.createScienceAlias());
        UserJpaEntity me = userJpaRepository.save(TestEntityFactory.createUser(a0, "me"));
        UserJpaEntity feedCreator = userJpaRepository.save(TestEntityFactory.createUser(a0, "feedCreator"));

        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook());        // 공통 Book

        // 피드 및 피드 태그 생성
        CategoryJpaEntity c1 = categoryJpaRepository.save(TestEntityFactory.createScienceCategory(a0));
        TagJpaEntity t1 = tagJpaRepository.save(TestEntityFactory.createTag(c1, Tag.BOOK_REPORT.getValue()));
        TagJpaEntity t2 = tagJpaRepository.save(TestEntityFactory.createTag(c1, Tag.BOOK_RECOMMEND.getValue()));
        FeedJpaEntity privateFeed = feedJpaRepository.save(TestEntityFactory.createFeed(feedCreator, book, false, 50, 10, List.of("content1", "content2")));      // feedCreator가 작성한 비공개 피드
        feedTagJpaRepository.save(TestEntityFactory.createFeedTagMapping(privateFeed, t1));
        feedTagJpaRepository.save(TestEntityFactory.createFeedTagMapping(privateFeed, t2));

        //when //then
        mockMvc.perform(get("/feeds/{feedId}", privateFeed.getPostId())
                        .requestAttr("userId", me.getUserId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString(FEED_CAN_NOT_SHOW_PRIVATE_ONE.getMessage())));
    }

    @Test
    @DisplayName("피드 작성자는 비공개 피드를 단일 조회할 수 있다.")
    void feed_can_show_private_one_by_feed_owner() throws Exception {
        //given
        AliasJpaEntity a0 = aliasJpaRepository.save(TestEntityFactory.createScienceAlias());
        UserJpaEntity me = userJpaRepository.save(TestEntityFactory.createUser(a0, "me"));

        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook());        // 공통 Book

        // 피드 및 피드 태그 생성
        CategoryJpaEntity c1 = categoryJpaRepository.save(TestEntityFactory.createScienceCategory(a0));
        TagJpaEntity t1 = tagJpaRepository.save(TestEntityFactory.createTag(c1, Tag.BOOK_REPORT.getValue()));
        TagJpaEntity t2 = tagJpaRepository.save(TestEntityFactory.createTag(c1, Tag.BOOK_RECOMMEND.getValue()));
        FeedJpaEntity privateFeed = feedJpaRepository.save(TestEntityFactory.createFeed(me, book, false, 50, 10, List.of("content1", "content2")));      // me가 작성한 비공개 피드
        feedTagJpaRepository.save(TestEntityFactory.createFeedTagMapping(privateFeed, t1));
        feedTagJpaRepository.save(TestEntityFactory.createFeedTagMapping(privateFeed, t2));

        //when //then
        mockMvc.perform(get("/feeds/{feedId}", privateFeed.getPostId())
                        .requestAttr("userId", me.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.feedId", is(privateFeed.getPostId().intValue())));
    }
}
