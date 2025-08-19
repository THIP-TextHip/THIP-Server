package konkuk.thip.common.util;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.jpa.SavedBookJpaEntity;
import konkuk.thip.comment.adapter.out.jpa.CommentJpaEntity;
import konkuk.thip.comment.adapter.out.jpa.CommentLikeJpaEntity;
import konkuk.thip.feed.adapter.out.jpa.FeedJpaEntity;
import konkuk.thip.feed.adapter.out.jpa.SavedFeedJpaEntity;
import konkuk.thip.feed.domain.Tag;
import konkuk.thip.feed.domain.TagList;
import konkuk.thip.feed.domain.value.ContentList;
import konkuk.thip.post.adapter.out.jpa.PostJpaEntity;
import konkuk.thip.post.adapter.out.jpa.PostLikeJpaEntity;
import konkuk.thip.post.domain.PostType;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomParticipantJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomParticipantRole;
import konkuk.thip.room.domain.Category;
import konkuk.thip.roompost.adapter.out.jpa.*;
import konkuk.thip.user.adapter.out.jpa.FollowingJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserRole;
import konkuk.thip.user.domain.Alias;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class TestEntityFactory {

    /**
     * 유효한 Jpa entity를 만들어주는 Factory
     */

    public static Alias createLiteratureAlias() {
        return Alias.WRITER;
    }

    public static Category createLiteratureCategory() {
        return Category.LITERATURE;
    }

    public static Alias createScienceAlias() {
        return Alias.SCIENTIST;
    }

    public static Category createScienceCategory() {
        return Category.SCIENCE_IT;
    }

    public static List<Tag> createTagsFromCategory(Category category) {
        return EnumMappings.tagsFrom(category);
    }

    public static UserJpaEntity createUser(Alias alias) {
        return UserJpaEntity.builder()
                .nickname("테스터")
                .nicknameUpdatedAt(LocalDate.now().minusMonths(7))
                .oauth2Id("kakao_12345678")
                .alias(alias)
                .role(UserRole.USER)
                .build();
    }

    public static UserJpaEntity createUser(Alias alias, String nickname) {
        return UserJpaEntity.builder()
                .nickname(nickname)
                .nicknameUpdatedAt(LocalDate.now().minusMonths(7))
                .oauth2Id("kakao_12345678")
                .alias(alias)
                .role(UserRole.USER)
                .build();
    }

    public static BookJpaEntity createBook() {
        return BookJpaEntity.builder()
                .title("책제목")
                .authorName("저자")
                .isbn(UUID.randomUUID().toString().replace("-", "").substring(0, 13))
                .bestSeller(false)
                .publisher("출판사")
                .imageUrl("img")
                .pageCount(100)
                .description("설명")
                .build();
    }

    /**
     * Book custom 생성자
     */
    public static BookJpaEntity createBook(int page) {
        return BookJpaEntity.builder()
                .title("책제목")
                .authorName("저자")
                .isbn(UUID.randomUUID().toString().replace("-", "").substring(0, 13))
                .bestSeller(false)
                .publisher("출판사")
                .imageUrl("img")
                .pageCount(page)
                .description("설명")
                .build();
    }

    public static BookJpaEntity createBookWithBookTitle(String bookTitle) {
        return BookJpaEntity.builder()
                .title(bookTitle)
                .authorName("저자")
                .isbn(UUID.randomUUID().toString().replace("-", "").substring(0, 13))
                .bestSeller(false)
                .publisher("출판사")
                .imageUrl("img")
                .pageCount(300)
                .description("설명")
                .build();
    }

    public static BookJpaEntity createBookWithISBN(String isbn) {
        return BookJpaEntity.builder()
                .title("책제목")
                .authorName("저자")
                .isbn(isbn)
                .bestSeller(false)
                .publisher("출판사")
                .imageUrl("img")
                .pageCount(100)
                .description("설명")
                .build();
    }

    public static RoomJpaEntity createRoom(BookJpaEntity book, Category category) {
        return RoomJpaEntity.builder()
                .title("방이름")
                .description("설명")
                .isPublic(true)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(5))
                .recruitCount(3)
                .bookJpaEntity(book)
                .category(category)
                .build();
    }

    public static RoomJpaEntity createCustomRoom(BookJpaEntity book, Category category, LocalDate startDate, LocalDate endDate) {
        return RoomJpaEntity.builder()
                .title("방이름")
                .description("설명")
                .isPublic(true)
                .startDate(startDate)
                .endDate(endDate)
                .recruitCount(3)
                .bookJpaEntity(book)
                .category(category)
                .build();
    }

    public static RoomJpaEntity createCustomRoom(BookJpaEntity book, Category category, String roomName, LocalDate startDate, LocalDate endDate) {
        return RoomJpaEntity.builder()
                .title(roomName)
                .description("설명")
                .isPublic(true)
                .startDate(startDate)
                .endDate(endDate)
                .recruitCount(20)
                .bookJpaEntity(book)
                .category(category)
                .build();
    }

    public static RoomParticipantJpaEntity createRoomParticipant(RoomJpaEntity room, UserJpaEntity user, RoomParticipantRole roomParticipantRole, double userPercentage) {
        return RoomParticipantJpaEntity.builder()
                .userJpaEntity(user)
                .roomJpaEntity(room)
                .roomParticipantRole(roomParticipantRole)
                .currentPage(0)
                .userPercentage(userPercentage)
                .build();
    }

    public static RecordJpaEntity createRecord(UserJpaEntity user, RoomJpaEntity room) {
        return RecordJpaEntity.builder()
                .content("기록 내용")
                .userJpaEntity(user)
                .page(22)
                .isOverview(false)
                .commentCount(0)
                .likeCount(0)
                .roomJpaEntity(room)
                .build();
    }

    public static VoteJpaEntity createVote(UserJpaEntity user, RoomJpaEntity room) {
        return VoteJpaEntity.builder()
                .content("투표 내용")
                .userJpaEntity(user)
                .page(33)
                .isOverview(true)
                .commentCount(0)
                .likeCount(0)
                .roomJpaEntity(room)
                .build();
    }

    public static VoteItemJpaEntity createVoteItem(String itemName, VoteJpaEntity vote) {
        return VoteItemJpaEntity.builder()
                .itemName(itemName)
                .count(0)
                .voteJpaEntity(vote)
                .build();
    }

    public static CommentJpaEntity createComment(PostJpaEntity post, UserJpaEntity user,PostType postType) {
        return CommentJpaEntity.builder()
                .content("댓글 내용")
                .postJpaEntity(post)
                .userJpaEntity(user)
                .likeCount(0)
                .reportCount(0)
                .postType(postType)
                .build();
    }

    /**
     * 댓글 내용, likeCount 커스텀
     */
    public static CommentJpaEntity createComment(PostJpaEntity post, UserJpaEntity user,PostType postType, String content, int likeCount) {
        return CommentJpaEntity.builder()
                .content(content)
                .postJpaEntity(post)
                .userJpaEntity(user)
                .likeCount(likeCount)
                .reportCount(0)
                .postType(postType)
                .build();
    }

    public static CommentJpaEntity createReplyComment(PostJpaEntity post, UserJpaEntity user,PostType postType,CommentJpaEntity parentComment) {
        return CommentJpaEntity.builder()
                .content("댓글 내용")
                .postJpaEntity(post)
                .userJpaEntity(user)
                .likeCount(0)
                .reportCount(0)
                .postType(postType)
                .parent(parentComment)
                .build();
    }

    /**
     * 자식 댓글 내용, likeCount 커스텀
     */
    public static CommentJpaEntity createReplyComment(PostJpaEntity post, UserJpaEntity user, PostType postType, CommentJpaEntity parentComment, String content, int likeCount) {
        return CommentJpaEntity.builder()
                .content(content)
                .postJpaEntity(post)
                .userJpaEntity(user)
                .likeCount(likeCount)
                .reportCount(0)
                .postType(postType)
                .parent(parentComment)
                .build();
    }

    public static CommentLikeJpaEntity createCommentLike(CommentJpaEntity comment, UserJpaEntity user) {
        return CommentLikeJpaEntity.builder()
                .userJpaEntity(user)
                .commentJpaEntity(comment)
                .build();
    }

    public static FollowingJpaEntity createFollowing(UserJpaEntity followerUser, UserJpaEntity followingUser) {
        return FollowingJpaEntity.builder()
                .userJpaEntity(followerUser)
                .followingUserJpaEntity(followingUser)
                .build();
    }

    /**
     * 공개/비공개 여부만을 설정하는 기본 피드 생성을 위한 팩토리 메서드
     */
    public static FeedJpaEntity createFeed(UserJpaEntity user, BookJpaEntity book, boolean isPublic) {

//        return FeedJpaEntity.builder()
//                .content("기본 피드 본문입니다.")
//                .isPublic(isPublic)
//                .likeCount(0)
//                .commentCount(0)
//                .reportCount(0)
//                .userJpaEntity(user)
//                .bookJpaEntity(book)
//                .contentList(ContentList.empty())
//                .build();
        return createFeed(user, book, isPublic, 0, 0, Collections.emptyList(), Collections.emptyList());
    }

    public static FeedJpaEntity createFeed(UserJpaEntity user, BookJpaEntity book, boolean isPublic, int likeCount, int commentCount, List<String> imageUrls) {

//        FeedJpaEntity feed = FeedJpaEntity.builder()
//                .content("이미지 포함 피드")
//                .isPublic(isPublic)
//                .likeCount(0)
//                .commentCount(0)
//                .reportCount(0)
//                .userJpaEntity(user)
//                .bookJpaEntity(book)
//                .contentList(ContentList.of(imageUrls))
//                .build();
//
        return createFeed(user, book, isPublic, likeCount, commentCount, imageUrls, Collections.emptyList());
    }

    public static FeedJpaEntity createFeed(UserJpaEntity user, BookJpaEntity book, List<String> imageUrls, boolean isPublic) {

//        FeedJpaEntity feed = FeedJpaEntity.builder()
//                .content("이미지 포함 피드")
//                .isPublic(isPublic)
//                .likeCount(0)
//                .commentCount(0)
//                .reportCount(0)
//                .userJpaEntity(user)
//                .bookJpaEntity(book)
//                .contentList(ContentList.of(imageUrls))
//                .build();
//
        return createFeed(user, book, isPublic, 0, 0, imageUrls, Collections.emptyList());
    }

    public static FeedJpaEntity createFeed(UserJpaEntity user, BookJpaEntity book, boolean isPublic, List<Tag> tags) {
        return createFeed(user, book, isPublic, 0, 0, Collections.emptyList(), tags);
    }

    /**
     * 커스텀 feed 생성을 위한 팩토리 메서드
     */
    public static FeedJpaEntity createFeed(UserJpaEntity user,
                                           BookJpaEntity book,
                                           boolean isPublic,
                                           int likeCount,
                                           int commentCount,
                                           List<String> imageUrls,
                                           List<Tag> tags
                                           ) {
        // 1) 기본 Feed 엔티티 빌드 (content, reportCount 등은 테스트용 기본값)
        FeedJpaEntity feed = FeedJpaEntity.builder()
                .content("기본 피드 본문입니다.")
                .isPublic(isPublic)
                .likeCount(likeCount)
                .commentCount(commentCount)
                .reportCount(0)
                .userJpaEntity(user)
                .bookJpaEntity(book)
                .contentList(ContentList.of(imageUrls))
                .tagList(TagList.of(tags))
                .build();

        return feed;
    }

    public static SavedFeedJpaEntity createSavedFeed(UserJpaEntity user, FeedJpaEntity feed) {
        return SavedFeedJpaEntity.builder()
                .feedJpaEntity(feed)
                .userJpaEntity(user)
                .build();
    }

    public static VoteParticipantJpaEntity createVoteParticipant(UserJpaEntity user, VoteItemJpaEntity item) {
        return VoteParticipantJpaEntity.builder()
                .userJpaEntity(user)
                .voteItemJpaEntity(item)
                .build();
    }

    public static PostLikeJpaEntity createPostLike(UserJpaEntity user, PostJpaEntity post) {
        return PostLikeJpaEntity.builder()
                .userJpaEntity(user)
                .postJpaEntity(post)
                .build();
    }

    public static SavedBookJpaEntity createSavedBook(UserJpaEntity user, BookJpaEntity book) {
        return SavedBookJpaEntity.builder()
                .userJpaEntity(user)
                .bookJpaEntity(book)
                .build();
    }

    public static AttendanceCheckJpaEntity createAttendanceCheck(String todayComment, RoomJpaEntity roomJpaEntity, UserJpaEntity userJpaEntity) {
        return AttendanceCheckJpaEntity.builder()
                .todayComment(todayComment)
                .roomJpaEntity(roomJpaEntity)
                .userJpaEntity(userJpaEntity)
                .build();
    }
}
