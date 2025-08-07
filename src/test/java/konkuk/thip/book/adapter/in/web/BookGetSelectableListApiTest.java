package konkuk.thip.book.adapter.in.web;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.book.adapter.out.persistence.repository.SavedBookJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.room.adapter.out.jpa.CategoryJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomParticipantRole;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.category.CategoryJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.roomparticipant.RoomParticipantJpaRepository;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
@DisplayName("[통합] 저장한 책 및 참여 중 책 리스트 조회 API 통합 테스트")
class BookGetSelectableListApiTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private AliasJpaRepository aliasJpaRepository;
    @Autowired private BookJpaRepository bookJpaRepository;
    @Autowired private SavedBookJpaRepository savedBookJpaRepository;
    @Autowired private CategoryJpaRepository categoryJpaRepository;
    @Autowired private RoomJpaRepository roomJpaRepository;
    @Autowired private RoomParticipantJpaRepository roomParticipantJpaRepository;

    private UserJpaEntity user;
    private BookJpaEntity savedBook;
    private BookJpaEntity joiningBook;

    @BeforeEach
    void setUp() {
        AliasJpaEntity alias = aliasJpaRepository.save(TestEntityFactory.createLiteratureAlias());
        user = userJpaRepository.save(TestEntityFactory.createUser(alias));
        savedBook = bookJpaRepository.save(TestEntityFactory.createBookWithISBN("1111111111111"));
        joiningBook = bookJpaRepository.save(TestEntityFactory.createBookWithISBN("2222222222222"));

        // SAVED 책 등록
        savedBookJpaRepository.save(TestEntityFactory.createSavedBook(user, savedBook));

        // JOINING 책용 방 생성
        CategoryJpaEntity category = categoryJpaRepository.save(TestEntityFactory.createLiteratureCategory(alias));
        RoomJpaEntity room = roomJpaRepository.save(TestEntityFactory.createRoom(joiningBook, category));
        roomParticipantJpaRepository.save(TestEntityFactory.createRoomParticipant(room, user, RoomParticipantRole.HOST, 0.0));
    }

    @Test
    @DisplayName("SAVED 타입 - 저장한 책 리스트 조회 성공")
    void getSelectableBooks_saved_success() throws Exception {
        mockMvc.perform(get("/books/selectable-list")
                        .param("type", "SAVED")
                        .requestAttr("userId", user.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bookList").isArray())
                .andExpect(jsonPath("$.data.bookList.length()").value(1))
                .andExpect(jsonPath("$.data.bookList[0].isbn").value(savedBook.getIsbn()));
    }

    @Test
    @DisplayName("JOINING 타입 - 참여 중인 책 리스트 조회 성공")
    void getSelectableBooks_joining_success() throws Exception {
        mockMvc.perform(get("/books/selectable-list")
                        .param("type", "JOINING")
                        .requestAttr("userId", user.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bookList").isArray())
                .andExpect(jsonPath("$.data.bookList.length()").value(2))
                .andExpect(jsonPath("$.data.bookList[0].isbn").value(joiningBook.getIsbn()));
    }
}