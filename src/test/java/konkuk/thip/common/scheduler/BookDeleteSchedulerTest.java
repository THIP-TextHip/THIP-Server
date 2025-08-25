package konkuk.thip.common.scheduler;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.book.adapter.out.persistence.repository.SavedBookJpaRepository;
import konkuk.thip.book.application.port.in.BookCleanUpUseCase;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.feed.adapter.out.persistence.repository.FeedJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("[통합] Book 삭제 스케줄러 기능 테스트")
class BookDeleteSchedulerTest {

    @Autowired
    private BookJpaRepository bookJpaRepository;

    @Autowired
    private RoomJpaRepository roomJpaRepository;

    @Autowired
    private FeedJpaRepository feedJpaRepository;

    @Autowired
    private SavedBookJpaRepository savedBookJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private BookCleanUpUseCase bookCleanUpUseCase;

    @Test
    @DisplayName("Room, Feed, SavedBook 어디에도 연결되지 않은 Book은 삭제된다")
    void deleteUnusedBooks_success() {
        // given
        // 사용되지 않는 Book
        BookJpaEntity unusedBook = bookJpaRepository.save(TestEntityFactory.createBookWithBookTitle("고아책"));

        // Room에 연결된 Book
        BookJpaEntity roomBook = bookJpaRepository.save(TestEntityFactory.createBookWithBookTitle("방책"));
        roomJpaRepository.save(TestEntityFactory.createRoom(roomBook, TestEntityFactory.createLiteratureCategory()));

        // Feed에 연결된 Book
        BookJpaEntity feedBook = bookJpaRepository.save(TestEntityFactory.createBookWithBookTitle("피드책"));
        UserJpaEntity feedUser = userJpaRepository.save(TestEntityFactory.createUser(TestEntityFactory.createLiteratureAlias()));
        feedJpaRepository.save(TestEntityFactory.createFeed(feedUser, feedBook, true));

        // SavedBook에 연결된 Book
        BookJpaEntity savedBook = bookJpaRepository.save(TestEntityFactory.createBookWithBookTitle("저장책"));
        UserJpaEntity savedUser = userJpaRepository.save(TestEntityFactory.createUser(TestEntityFactory.createLiteratureAlias(), "저장유저"));
        savedBookJpaRepository.save(TestEntityFactory.createSavedBook(savedUser, savedBook));

        // when
        bookCleanUpUseCase.deleteUnusedBooks();

        // then
        List<BookJpaEntity> remainingBooks = bookJpaRepository.findAll();

        // 삭제되지 않은 책의 제목만 수집
        List<String> remainingTitles = remainingBooks.stream().map(BookJpaEntity::getTitle).toList();

        assertThat(remainingTitles).contains("방책", "피드책", "저장책");
        assertThat(remainingTitles).doesNotContain("고아책");
    }
}