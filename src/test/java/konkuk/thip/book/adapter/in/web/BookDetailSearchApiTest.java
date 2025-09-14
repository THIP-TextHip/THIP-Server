package konkuk.thip.book.adapter.in.web;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.book.application.service.BookSearchService;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.feed.adapter.out.jpa.FeedJpaEntity;
import konkuk.thip.feed.adapter.out.persistence.repository.FeedJpaRepository;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomParticipantJpaEntity;
import konkuk.thip.room.domain.value.RoomParticipantRole;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.roomparticipant.RoomParticipantJpaRepository;
import konkuk.thip.book.adapter.out.jpa.SavedBookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.SavedBookJpaRepository;
import konkuk.thip.room.domain.value.Category;
import konkuk.thip.room.domain.value.RoomStatus;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.domain.value.UserRole;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.domain.value.Alias;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("[통합] 책 상세보기 api 통합 테스트")
class BookDetailSearchApiTest {

    @Autowired
    private BookSearchService bookSearchService;
    @Autowired
    private UserJpaRepository userJpaRepository;
    @Autowired
    private BookJpaRepository bookJpaRepository;
    @Autowired
    private RoomJpaRepository roomJpaRepository;
    @Autowired
    private RoomParticipantJpaRepository roomParticipantJpaRepository;
    @Autowired
    private FeedJpaRepository feedJpaRepository;
    @Autowired
    private SavedBookJpaRepository savedBookJpaRepository;

    @BeforeEach
    void setup() {
        Alias alias = TestEntityFactory.createLiteratureAlias();

        UserJpaEntity user = userJpaRepository.save(UserJpaEntity.builder()
                .oauth2Id("kakao_432708231")
                .nickname("User1")
                .nicknameUpdatedAt(LocalDate.now().minusMonths(7))
                .role(UserRole.USER)
                .alias(alias)
                .build());

        BookJpaEntity book = bookJpaRepository.save(BookJpaEntity.builder()
                .title("작별하지 않는다")
                .isbn("9791168342941")
                .authorName("한강")
                .bestSeller(false)
                .publisher("문학동네")
                .imageUrl("https://image1.jpg")
                .pageCount(300)
                .description("한강의 소설")
                .build());

        Category category = TestEntityFactory.createLiteratureCategory();

        RoomJpaEntity room = roomJpaRepository.save(RoomJpaEntity.builder()
                .title("한강 독서모임")
                .description("한강 작품 읽기 모임")
                .isPublic(true)
                .roomPercentage(0.0)
                .startDate(LocalDate.now().plusDays(2))
                .endDate(LocalDate.now().plusDays(30))
                .recruitCount(10)
                .bookJpaEntity(book)
                .category(category)
                .build());

        roomParticipantJpaRepository.save(RoomParticipantJpaEntity.builder()
                .currentPage(10)
                .userPercentage(10.0)
                .roomParticipantRole(RoomParticipantRole.MEMBER)
                .userJpaEntity(user)
                .roomJpaEntity(room)
                .build());

        feedJpaRepository.save(FeedJpaEntity.builder()
                .content("한강 책 너무 좋아요!")
                .userJpaEntity(user)
                .isPublic(true)
                .reportCount(0)
                .bookJpaEntity(book)
                .build());

        savedBookJpaRepository.save(SavedBookJpaEntity.builder()
                .userJpaEntity(user)
                .bookJpaEntity(book)
                .build());
    }

    @AfterEach
    void tearDown() {
        savedBookJpaRepository.deleteAllInBatch();
        feedJpaRepository.deleteAllInBatch();
        roomParticipantJpaRepository.deleteAllInBatch();
        roomJpaRepository.deleteAllInBatch();
        bookJpaRepository.deleteAllInBatch();
        userJpaRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("책 상세 검색 결과를 정상적으로 반환.")
    void searchDetailBooks_ReturnsCorrectResult() {
        String isbn = "9791168342941";
        UserJpaEntity user = userJpaRepository.findAll().get(0);

        var result = bookSearchService.searchDetailBooks(isbn, user.getUserId());

        assertThat(result).isNotNull();
        assertThat(result.recruitingRoomCount()).isEqualTo(1);
        assertThat(result.readCount()).isEqualTo(1);
        assertThat(result.isSaved()).isTrue();
        assertThat(result.naverDetailBook()).isNotNull();
    }

    @Test
    @DisplayName("모집 중인 방이 없으면 recruitingRoomCount가 0")
    void searchDetailBooks_NoRecruitingRooms_ReturnsZero() {
        String isbn = "9791168342941";
        UserJpaEntity user = userJpaRepository.findAll().get(0);
        BookJpaEntity book = bookJpaRepository.findAll().get(0);

        // 기존 방 삭제
        roomParticipantJpaRepository.deleteAllInBatch();
        roomJpaRepository.deleteAll();

        // startDate가 과거인 새로운 방 생성 (모집 중 아님)
        RoomJpaEntity pastRoom = RoomJpaEntity.builder()
                .title("과거방")
                .description("모집기간이 지난 방")
                .isPublic(true)
                .roomPercentage(0.0)
                .startDate(LocalDate.now().minusDays(10))
                .endDate(LocalDate.now().minusDays(5))
                .roomStatus(RoomStatus.IN_PROGRESS)
                .recruitCount(10)
                .bookJpaEntity(book)
                .category(Category.LITERATURE)
                .build();
        roomJpaRepository.save(pastRoom);

        var result = bookSearchService.searchDetailBooks(isbn, user.getUserId());
        assertThat(result.recruitingRoomCount()).isEqualTo(0);
    }


    @Test
    @DisplayName("피드와 방 참여자가 모두 없으면 readCount가 0")
    void searchDetailBooks_NoFeedOrRoomParticipants_ReturnsZero() {
        String isbn = "9791168342941";
        UserJpaEntity user = userJpaRepository.findAll().get(0);

        feedJpaRepository.deleteAllInBatch();
        roomParticipantJpaRepository.deleteAllInBatch();

        var result = bookSearchService.searchDetailBooks(isbn, user.getUserId());
        assertThat(result.readCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("사용자가 책을 저장하지 않았으면 isSaved가 false")
    void searchDetailBooks_BookNotSaved_ReturnsFalse() {
        String isbn = "9791168342941";
        UserJpaEntity user = userJpaRepository.findAll().get(0);

        savedBookJpaRepository.deleteAll();

        var result = bookSearchService.searchDetailBooks(isbn, user.getUserId());
        assertThat(result.isSaved()).isFalse();
    }
}
