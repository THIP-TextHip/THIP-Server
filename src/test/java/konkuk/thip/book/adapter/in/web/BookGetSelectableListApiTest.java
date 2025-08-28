package konkuk.thip.book.adapter.in.web;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.jpa.SavedBookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.book.adapter.out.persistence.repository.SavedBookJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomParticipantJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomParticipantRole;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.roomparticipant.RoomParticipantJpaRepository;
import konkuk.thip.room.domain.value.Category;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.domain.value.Alias;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("[통합] 저장한 책 및 참여 중 책 리스트 조회 API 통합 테스트")
class BookGetSelectableListApiTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private BookJpaRepository bookJpaRepository;
    @Autowired private SavedBookJpaRepository savedBookJpaRepository;
    @Autowired private RoomJpaRepository roomJpaRepository;
    @Autowired private RoomParticipantJpaRepository roomParticipantJpaRepository;

    @Autowired private JdbcTemplate jdbcTemplate;

    @AfterEach
    void tearDown() {
        roomParticipantJpaRepository.deleteAllInBatch();
        roomJpaRepository.deleteAll();
        savedBookJpaRepository.deleteAllInBatch();
        bookJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
    }

    private RoomJpaEntity saveScienceRoomWithBookIsbn(String isbn, String roomName, double roomPercentage) {
        BookJpaEntity book = bookJpaRepository.save(BookJpaEntity.builder()
                .title("책이름")
                .isbn(isbn)
                .authorName("한강")
                .bestSeller(false)
                .publisher("문학동네")
                .imageUrl("https://image1.jpg")
                .pageCount(300)
                .description("한강의 소설")
                .build());

        Category category = TestEntityFactory.createScienceCategory();

        return roomJpaRepository.save(RoomJpaEntity.builder()
                .title(roomName)
                .description("한강 작품 읽기 모임")
                .isPublic(true)
                .roomPercentage(roomPercentage)
                .startDate(LocalDate.now().minusDays(2)) // 진행중인 방
                .endDate(LocalDate.now().plusDays(10))
                .recruitCount(5)
                .bookJpaEntity(book)
                .category(category)
                .build());
    }

    private RoomJpaEntity saveScienceRoomWithBookEntity(BookJpaEntity book, String roomName, double roomPercentage) {
        Category category = TestEntityFactory.createScienceCategory();

        return roomJpaRepository.save(RoomJpaEntity.builder()
                .title(roomName)
                .description("한강 작품 읽기 모임")
                .isPublic(true)
                .roomPercentage(roomPercentage)
                .startDate(LocalDate.now().minusDays(2)) // 진행중인 방
                .endDate(LocalDate.now().plusDays(10))
                .recruitCount(5)
                .bookJpaEntity(book)
                .category(category)
                .build());
    }

    private void saveSingleUserToRoom(RoomJpaEntity roomJpaEntity, UserJpaEntity userJpaEntity) {
        RoomParticipantJpaEntity roomParticipantJpaEntity = RoomParticipantJpaEntity.builder()
                .userJpaEntity(userJpaEntity)
                .roomJpaEntity(roomJpaEntity)
                .roomParticipantRole(RoomParticipantRole.HOST)
                .userPercentage(0.0)
                .build();
        roomParticipantJpaRepository.save(roomParticipantJpaEntity);
        roomJpaEntity.updateMemberCount(roomJpaEntity.getMemberCount() + 1);
        roomJpaRepository.save(roomJpaEntity);

    }

    @Test
    @DisplayName("SAVED 타입 - 저장된 책 조회 시 책 정보를 저장한 최신순으로 정렬해서 반환한다.")
    void getSelectableBooks_saved_success() throws Exception {

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
        mockMvc.perform(get("/books/selectable-list")
                        .param("type", "SAVED")
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
                // b2
                .andExpect(jsonPath("$.data.bookList[1].bookId").value(b2.getBookId().intValue()))
                .andExpect(jsonPath("$.data.bookList[1].bookTitle").value(b2.getTitle()))
                .andExpect(jsonPath("$.data.bookList[1].authorName").value(b2.getAuthorName()))
                .andExpect(jsonPath("$.data.bookList[1].publisher").value(b2.getPublisher()))
                .andExpect(jsonPath("$.data.bookList[1].bookImageUrl").value(b2.getImageUrl()))
                .andExpect(jsonPath("$.data.bookList[1].isbn").value(b2.getIsbn()));
    }

    @Test
    @DisplayName("SAVED 타입 - 저장한 책 조회 시, 한번에 최대 10개의 데이터만을 반환한다." +
            "다음 페이지에 해당하는 데이터가 있을 경우, 다음 페이지의 cursor 값을 반환한다." +
            "또한 cursor 값을 기준으로 해당 페이지의 데이터를 반환한다.")
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

    @Test
    @DisplayName("JOINING 타입 - 참여 중인 방의 책 조회 시 방의 진행도(내림차순), 책아이디(오름차순)으로 정렬해서 반환한다.")
    void getSelectableBooks_joining_success() throws Exception {

        // given
        Alias alias = TestEntityFactory.createLiteratureAlias();
        UserJpaEntity me = userJpaRepository.save(TestEntityFactory.createUser(alias));
        RoomJpaEntity r1 = saveScienceRoomWithBookIsbn("isbn1","모집중인방1" , 80.0);
        RoomJpaEntity r2 = saveScienceRoomWithBookIsbn("isbn2","모집중인방2" , 60.0); // 책아이디2
        RoomJpaEntity r3 = saveScienceRoomWithBookIsbn("isbn3","모집중인방3" , 60.0); // 책아이디3

        saveSingleUserToRoom(r1,me);
        saveSingleUserToRoom(r2,me);
        saveSingleUserToRoom(r3,me);

        //when & then
        mockMvc.perform(get("/books/selectable-list")
                        .param("type", "JOINING")
                        .requestAttr("userId", me.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isLast", is(true)))
                .andExpect(jsonPath("$.data.bookList", hasSize(3)))
                .andExpect(jsonPath("$.data.nextCursor").value(Matchers.nullValue()))
                // 방 진행도 높은 순 -> 진행도 같으면 책 아이디 작은 순
                // 방 진행도 높은순 방1이 80로 진행도 제일 높음
                .andExpect(jsonPath("$.data.bookList[0].bookId").value(r1.getBookJpaEntity().getBookId().intValue()))
                .andExpect(jsonPath("$.data.bookList[0].bookTitle").value(r1.getBookJpaEntity().getTitle()))
                .andExpect(jsonPath("$.data.bookList[0].authorName").value(r1.getBookJpaEntity().getAuthorName()))
                .andExpect(jsonPath("$.data.bookList[0].publisher").value(r1.getBookJpaEntity().getPublisher()))
                .andExpect(jsonPath("$.data.bookList[0].bookImageUrl").value(r1.getBookJpaEntity().getImageUrl()))
                .andExpect(jsonPath("$.data.bookList[0].isbn").value(r1.getBookJpaEntity().getIsbn()))
                // 방2,방3 진행도 60으로 같아서 책아이디 작은 방2
                .andExpect(jsonPath("$.data.bookList[1].bookId").value(r2.getBookJpaEntity().getBookId().intValue()))
                .andExpect(jsonPath("$.data.bookList[1].bookTitle").value(r2.getBookJpaEntity().getTitle()))
                .andExpect(jsonPath("$.data.bookList[1].authorName").value(r2.getBookJpaEntity().getAuthorName()))
                .andExpect(jsonPath("$.data.bookList[1].publisher").value(r2.getBookJpaEntity().getPublisher()))
                .andExpect(jsonPath("$.data.bookList[1].bookImageUrl").value(r2.getBookJpaEntity().getImageUrl()))
                .andExpect(jsonPath("$.data.bookList[1].isbn").value(r2.getBookJpaEntity().getIsbn()))
                // 방2,방3 진행도 60으로 같아서 마지막으로 방3
                .andExpect(jsonPath("$.data.bookList[2].bookId").value(r3.getBookJpaEntity().getBookId().intValue()))
                .andExpect(jsonPath("$.data.bookList[2].bookTitle").value(r3.getBookJpaEntity().getTitle()))
                .andExpect(jsonPath("$.data.bookList[2].authorName").value(r3.getBookJpaEntity().getAuthorName()))
                .andExpect(jsonPath("$.data.bookList[2].publisher").value(r3.getBookJpaEntity().getPublisher()))
                .andExpect(jsonPath("$.data.bookList[2].bookImageUrl").value(r3.getBookJpaEntity().getImageUrl()))
                .andExpect(jsonPath("$.data.bookList[2].isbn").value(r3.getBookJpaEntity().getIsbn()));
    }

    @Test
    @DisplayName("JOINING 타입 - 참여 중인 방의 책 조회 시 동일한 책으로 방이 여러개있을때 가장 높은 방의 진행도만 반영되어 정렬해서 반환한다.")
    void shouldReturnBooksOrderedByHighestRoomPercentage_withCursorPagination() throws Exception {

        // given
        Alias alias = TestEntityFactory.createLiteratureAlias();
        UserJpaEntity me = userJpaRepository.save(TestEntityFactory.createUser(alias));
        BookJpaEntity b1 = bookJpaRepository.save(TestEntityFactory.createBook());
        BookJpaEntity b2 = bookJpaRepository.save(TestEntityFactory.createBook());
        RoomJpaEntity r1 = saveScienceRoomWithBookEntity(b1,"모집중인방1" , 80.0);
        RoomJpaEntity r2 = saveScienceRoomWithBookEntity(b2,"모집중인방1" , 60.0);
        RoomJpaEntity r3 = saveScienceRoomWithBookEntity(b2,"모집중인방1" , 90.0);
        saveSingleUserToRoom(r1,me);
        saveSingleUserToRoom(r2,me);
        saveSingleUserToRoom(r3,me);

        //when & then
        mockMvc.perform(get("/books/selectable-list")
                        .param("type", "JOINING")
                        .requestAttr("userId", me.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isLast", is(true)))
                .andExpect(jsonPath("$.data.bookList", hasSize(2)))
                .andExpect(jsonPath("$.data.nextCursor").value(Matchers.nullValue()))
                // 방 진행도 높은 순 -> 진행도 같으면 책 아이디 작은 순
                // 방2의 진행도(60)가 방1 진행도(80)보다 낮지만 방2와 동일한 책인 방3의 진행도(90)가 방1 진행도 보다 높기때문에 책2가 제일먼저 오게됨
                .andExpect(jsonPath("$.data.bookList[0].bookId").value(r3.getBookJpaEntity().getBookId().intValue()))
                .andExpect(jsonPath("$.data.bookList[0].bookTitle").value(r3.getBookJpaEntity().getTitle()))
                .andExpect(jsonPath("$.data.bookList[0].authorName").value(r3.getBookJpaEntity().getAuthorName()))
                .andExpect(jsonPath("$.data.bookList[0].publisher").value(r3.getBookJpaEntity().getPublisher()))
                .andExpect(jsonPath("$.data.bookList[0].bookImageUrl").value(r3.getBookJpaEntity().getImageUrl()))
                .andExpect(jsonPath("$.data.bookList[0].isbn").value(r3.getBookJpaEntity().getIsbn()))
                // 책1이 마지막에 조회된다.
                .andExpect(jsonPath("$.data.bookList[1].bookId").value(r1.getBookJpaEntity().getBookId().intValue()))
                .andExpect(jsonPath("$.data.bookList[1].bookTitle").value(r1.getBookJpaEntity().getTitle()))
                .andExpect(jsonPath("$.data.bookList[1].authorName").value(r1.getBookJpaEntity().getAuthorName()))
                .andExpect(jsonPath("$.data.bookList[1].publisher").value(r1.getBookJpaEntity().getPublisher()))
                .andExpect(jsonPath("$.data.bookList[1].bookImageUrl").value(r1.getBookJpaEntity().getImageUrl()))
                .andExpect(jsonPath("$.data.bookList[1].isbn").value(r1.getBookJpaEntity().getIsbn()));
    }

    @Test
    @DisplayName("JOINING 타입 - 참여 중인 방의 책 조회 시, 한번에 최대 10개의 데이터만을 반환한다." +
            "다음 페이지에 해당하는 데이터가 있을 경우, 다음 페이지의 cursor 값을 반환한다." +
            "또한 cursor 값을 기준으로 해당 페이지의 데이터를 반환한다.")
    void getSelectableBooks_joining_pageWithCursor() throws Exception {

        // given
        Alias alias = TestEntityFactory.createLiteratureAlias();
        UserJpaEntity me = userJpaRepository.save(TestEntityFactory.createUser(alias));

        // when
        // 방,책 12개 생성 및 저장
        for (int i = 1; i < 13; i++) {
            String isbn = "isbn" + i;
            String title = "모집중인방" + i;
            double roomPercentage = 90 - i; // 진행률은 방번호가 작을수록 높음
            RoomJpaEntity room = saveScienceRoomWithBookIsbn(isbn ,title, roomPercentage);
            saveSingleUserToRoom(room,me);
        }

        //then
        // 1. 첫 페이지 조회 (size=10), 커서 없음
        ResultActions firstPage =  mockMvc.perform(get("/books/selectable-list")
                .param("type", "JOINING")
                .requestAttr("userId", me.getUserId()));

        firstPage.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bookList", hasSize(10)))
                .andExpect(jsonPath("$.data.nextCursor").exists())
                .andExpect(jsonPath("$.data.isLast").value(false))
                // 방 진행도 높은 순으로 조회
                .andExpect(jsonPath("$.data.bookList[0].isbn", is("isbn1")))
                .andExpect(jsonPath("$.data.bookList[1].isbn", is("isbn2")))
                .andExpect(jsonPath("$.data.bookList[2].isbn", is("isbn3")))
                .andExpect(jsonPath("$.data.bookList[3].isbn", is("isbn4")))
                .andExpect(jsonPath("$.data.bookList[4].isbn", is("isbn5")))
                .andExpect(jsonPath("$.data.bookList[5].isbn", is("isbn6")))
                .andExpect(jsonPath("$.data.bookList[6].isbn", is("isbn7")))
                .andExpect(jsonPath("$.data.bookList[7].isbn", is("isbn8")))
                .andExpect(jsonPath("$.data.bookList[8].isbn", is("isbn9")))
                .andExpect(jsonPath("$.data.bookList[9].isbn", is("isbn10")));


        String responseBody = firstPage.andReturn().getResponse().getContentAsString();
        String nextCursor = com.jayway.jsonpath.JsonPath.read(responseBody, "$.data.nextCursor");

        // when
        // 2. 두 번째 페이지 조회(커서 사용, size=10)
        ResultActions secondPage = mockMvc.perform(get("/books/selectable-list")
                .requestAttr("userId", me.getUserId())
                .param("type", "JOINING")
                .param("cursor", nextCursor) // 이전에 b9 까지 조회 -> b9의 방 r9의 진행도 + b9의 bookId가 커서
        );

        //then
        secondPage.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bookList", hasSize(2)))
                .andExpect(jsonPath("$.data.nextCursor").value(Matchers.nullValue())) // nextCursor 는 null
                .andExpect(jsonPath("$.data.isLast").value(true))
                .andExpect(jsonPath("$.data.bookList[0].isbn", is("isbn11")))
                .andExpect(jsonPath("$.data.bookList[1].isbn", is("isbn12")));

    }
}
