package konkuk.thip.book.adapter.in.web;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.jpa.SavedBookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.book.adapter.out.persistence.repository.SavedBookJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.roomparticipant.RoomParticipantJpaRepository;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.domain.value.Alias;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
@DisplayName("[통합] 저장한 책 조회 API 통합 테스트")
class BookShowSavedListApiTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private BookJpaRepository bookJpaRepository;
    @Autowired private SavedBookJpaRepository savedBookJpaRepository;
    @Autowired private RoomJpaRepository roomJpaRepository;
    @Autowired private RoomParticipantJpaRepository roomParticipantJpaRepository;

    @Autowired private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("저장된 책 조회 시 책 정보를 저장한 최신순으로 정렬해서 반환한다.")
    void getSavedBooks_success() throws Exception {

        // given
        Alias alias = TestEntityFactory.createScienceAlias();
        UserJpaEntity me = userJpaRepository.save(TestEntityFactory.createUser(alias, "me"));
        BookJpaEntity b1 = bookJpaRepository.save(TestEntityFactory.createBook());
        BookJpaEntity b2 = bookJpaRepository.save(TestEntityFactory.createBook());

        // when
        // me가 b1,b2 저장
        SavedBookJpaEntity sb1 = savedBookJpaRepository.save(TestEntityFactory.createSavedBook(me, b1));
        SavedBookJpaEntity sb2 = savedBookJpaRepository.save(TestEntityFactory.createSavedBook(me, b2));

        // flush 후 책 저장일자 덮어쓰기
        // 책 저장 순서 : b2 -> b1 (b1 이 가장 최신)
        LocalDateTime baseTime = LocalDateTime.now();
        savedBookJpaRepository.flush();
        jdbcTemplate.update("UPDATE saved_books SET created_at = ? WHERE saved_id = ?",
                Timestamp.valueOf(baseTime.minusMinutes(1)), sb1.getSavedId());
        jdbcTemplate.update("UPDATE saved_books SET created_at = ? WHERE saved_id = ?",
                Timestamp.valueOf(baseTime.minusMinutes(10)), sb2.getSavedId());

        // then
        mockMvc.perform(get("/books/saved")
                        .requestAttr("userId", me.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isLast", is(true)))
                .andExpect(jsonPath("$.data.bookList", hasSize(2)))
                .andExpect(jsonPath("$.data.nextCursor").value(Matchers.nullValue()))
                // 저장한 최신순 -> b1 먼저
                .andExpect(jsonPath("$.data.bookList[0].bookId").value(b1.getBookId().intValue()))
                .andExpect(jsonPath("$.data.bookList[0].bookTitle").value(b1.getTitle()))
                .andExpect(jsonPath("$.data.bookList[0].authorName").value(b1.getAuthorName()))
                .andExpect(jsonPath("$.data.bookList[0].publisher").value(b1.getPublisher()))
                .andExpect(jsonPath("$.data.bookList[0].bookImageUrl").value(b1.getImageUrl()))
                .andExpect(jsonPath("$.data.bookList[0].isbn").value(b1.getIsbn()))
                .andExpect(jsonPath("$.data.bookList[0].isSaved", is(true)))
                // b2
                .andExpect(jsonPath("$.data.bookList[1].bookId").value(b2.getBookId().intValue()))
                .andExpect(jsonPath("$.data.bookList[1].bookTitle").value(b2.getTitle()))
                .andExpect(jsonPath("$.data.bookList[1].authorName").value(b2.getAuthorName()))
                .andExpect(jsonPath("$.data.bookList[1].publisher").value(b2.getPublisher()))
                .andExpect(jsonPath("$.data.bookList[1].bookImageUrl").value(b2.getImageUrl()))
                .andExpect(jsonPath("$.data.bookList[1].isbn").value(b2.getIsbn()))
                .andExpect(jsonPath("$.data.bookList[1].isSaved", is(true)));
    }

    @Test
    @DisplayName("한번에 최대 10개의 데이터만을 반환한다. 다음 페이지에 해당하는 데이터가 있을 경우, 다음 페이지의 cursor 값을 반환한다. 또한 cursor 값을 기준으로 해당 페이지의 데이터를 반환한다.")
    void getSavedBooks_pageWithCursor() throws Exception {

        // given
        Alias alias = TestEntityFactory.createLiteratureAlias();
        UserJpaEntity me = userJpaRepository.save(TestEntityFactory.createUser(alias));

        // when
        // 책 12개 생성 및 저장
        LocalDateTime baseTime = LocalDateTime.now();
        BookJpaEntity[] books = new BookJpaEntity[12];
        SavedBookJpaEntity[] savedBooks = new SavedBookJpaEntity[12];

        for (int i = 0; i < 12; i++) {
            BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook());
            books[i] = book;
            SavedBookJpaEntity savedBook = savedBookJpaRepository.save(
                    SavedBookJpaEntity.builder()
                            .userJpaEntity(me)
                            .bookJpaEntity(book)
                            .build()
            );
            savedBooks[i] = savedBook;
        }
        savedBookJpaRepository.flush();

        // created_at 덮어쓰기 bookId가 작을수록 최신 저장순
        for (int i = 0; i < 12; i++) {
            jdbcTemplate.update("UPDATE saved_books SET created_at = ? WHERE saved_id = ?",
                    Timestamp.valueOf(baseTime.minusMinutes(i)), savedBooks[i].getSavedId());
        }

        //then
        // 1. 첫 페이지 조회 (size=10), 커서 없음
        ResultActions firstPage = mockMvc.perform(get("/books/saved")
                .requestAttr("userId", me.getUserId())
        );

        firstPage.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bookList", hasSize(10)))
                .andExpect(jsonPath("$.data.nextCursor").exists())
                .andExpect(jsonPath("$.data.isLast").value(false))
                //책을 저장한 순으로 조회
                .andExpect(jsonPath("$.data.bookList[0].bookId", is(books[0].getBookId().intValue())))
                .andExpect(jsonPath("$.data.bookList[1].bookId", is(books[1].getBookId().intValue())))
                .andExpect(jsonPath("$.data.bookList[2].bookId", is(books[2].getBookId().intValue())))
                .andExpect(jsonPath("$.data.bookList[3].bookId", is(books[3].getBookId().intValue())))
                .andExpect(jsonPath("$.data.bookList[4].bookId", is(books[4].getBookId().intValue())))
                .andExpect(jsonPath("$.data.bookList[5].bookId", is(books[5].getBookId().intValue())))
                .andExpect(jsonPath("$.data.bookList[6].bookId", is(books[6].getBookId().intValue())))
                .andExpect(jsonPath("$.data.bookList[7].bookId", is(books[7].getBookId().intValue())))
                .andExpect(jsonPath("$.data.bookList[8].bookId", is(books[8].getBookId().intValue())))
                .andExpect(jsonPath("$.data.bookList[9].bookId", is(books[9].getBookId().intValue())));


        String responseBody = firstPage.andReturn().getResponse().getContentAsString();
        String nextCursor = com.jayway.jsonpath.JsonPath.read(responseBody, "$.data.nextCursor");

        // when
        // 2. 두 번째 페이지 조회(커서 사용, size=10)
        ResultActions secondPage = mockMvc.perform(get("/books/saved")
                .requestAttr("userId", me.getUserId())
                .param("cursor", nextCursor) // 이전에 b9 까지 조회 -> b9의 저장시간, sb9의 createdAt이 커서
        );

        //then
        secondPage.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bookList", hasSize(2)))
                .andExpect(jsonPath("$.data.nextCursor").value(Matchers.nullValue())) // nextCursor 는 null
                .andExpect(jsonPath("$.data.isLast").value(true))
                .andExpect(jsonPath("$.data.bookList[0].bookId", is(books[10].getBookId().intValue())))
                .andExpect(jsonPath("$.data.bookList[1].bookId", is(books[11].getBookId().intValue())));

    }

}
