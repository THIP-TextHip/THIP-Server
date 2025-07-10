package konkuk.thip.common.util;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.record.adapter.out.jpa.RecordJpaEntity;
import konkuk.thip.room.adapter.out.jpa.CategoryJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.user.adapter.out.jpa.AliasJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserRole;
import konkuk.thip.vote.adapter.out.jpa.VoteJpaEntity;
import konkuk.thip.comment.adapter.out.jpa.CommentJpaEntity;
import konkuk.thip.post.adapter.out.jpa.PostJpaEntity;

import java.time.LocalDate;

public class TestEntityFactory {

    /**
     * 유효한 Jpa entity를 만들어주는 Factory
     */

    public static AliasJpaEntity createLiteratureAlias() {
        return AliasJpaEntity.builder()         // 실제 존재하는 값으로
                .value("문학가")
                .imageUrl("문학_image")
                .color("문학_color")
                .build();
    }

    public static CategoryJpaEntity createLiteratureCategory(AliasJpaEntity alias) {
        return CategoryJpaEntity.builder()      // 실제 존재하는 값으로
                .value("문학")
                .aliasForCategoryJpaEntity(alias)
                .build();
    }

    public static AliasJpaEntity createScienceAlias() {
        return AliasJpaEntity.builder()         // 실제 존재하는 값으로
                .value("과학자")
                .imageUrl("과학_image")
                .color("과학_color")
                .build();
    }

    public static CategoryJpaEntity createScienceCategory(AliasJpaEntity alias) {
        return CategoryJpaEntity.builder()      // 실제 존재하는 값으로
                .value("과학/IT")
                .aliasForCategoryJpaEntity(alias)
                .build();
    }

    public static UserJpaEntity createUser(AliasJpaEntity alias) {
        return UserJpaEntity.builder()
                .nickname("테스터")
                .imageUrl("https://test.img")
                .oauth2Id("kakao_12345678")
                .aliasForUserJpaEntity(alias)
                .role(UserRole.USER)
                .build();
    }

    public static BookJpaEntity createBook() {
        return BookJpaEntity.builder()
                .title("책제목")
                .authorName("저자")
                .isbn("isbn")
                .bestSeller(false)
                .publisher("출판사")
                .imageUrl("img")
                .pageCount(100)
                .description("설명")
                .build();
    }

    public static RoomJpaEntity createRoom(BookJpaEntity book, CategoryJpaEntity category) {
        return RoomJpaEntity.builder()
                .title("방이름")
                .description("설명")
                .isPublic(true)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(5))
                .recruitCount(3)
                .bookJpaEntity(book)
                .categoryJpaEntity(category)
                .build();
    }

    public static RecordJpaEntity createRecord(UserJpaEntity user, RoomJpaEntity room) {
        return RecordJpaEntity.builder()
                .content("기록 내용")
                .userJpaEntity(user)
                .page(22)
                .isOverview(false)
                .roomJpaEntity(room)
                .build();
    }

    public static VoteJpaEntity createVote(UserJpaEntity user, RoomJpaEntity room) {
        return VoteJpaEntity.builder()
                .content("투표 내용")
                .userJpaEntity(user)
                .page(33)
                .isOverview(true)
                .roomJpaEntity(room)
                .build();
    }

    public static CommentJpaEntity createComment(PostJpaEntity post, UserJpaEntity user) {
        return CommentJpaEntity.builder()
                .content("댓글 내용")
                .postJpaEntity(post)
                .userJpaEntity(user)
                .build();
    }
}