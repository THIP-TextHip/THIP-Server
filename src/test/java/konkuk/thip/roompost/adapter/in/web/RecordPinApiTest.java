package konkuk.thip.roompost.adapter.in.web;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.room.domain.value.Category;
import konkuk.thip.roompost.adapter.out.jpa.RecordJpaEntity;
import konkuk.thip.roompost.adapter.out.persistence.repository.record.RecordJpaRepository;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.domain.value.RoomParticipantRole;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.roomparticipant.RoomParticipantJpaRepository;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.domain.value.Alias;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[통합] 기록을 핀하기 api 통합 테스트")
class RecordPinApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private BookJpaRepository bookJpaRepository;
    @Autowired private RecordJpaRepository recordJpaRepository;
    @Autowired private RoomJpaRepository roomJpaRepository;
    @Autowired private RoomParticipantJpaRepository roomParticipantJpaRepository;

    private Alias alias;
    private UserJpaEntity user;
    private Category category;
    private BookJpaEntity book;
    private RecordJpaEntity record;
    private RoomJpaEntity room;

    @BeforeEach
    void setUp() {
        alias = TestEntityFactory.createLiteratureAlias();
        user = userJpaRepository.save(TestEntityFactory.createUser(alias));
        category = TestEntityFactory.createLiteratureCategory();
        book = bookJpaRepository.save(TestEntityFactory.createBookWithISBN("9788954682152"));
        room = roomJpaRepository.save(TestEntityFactory.createRoom(book,category));
        record = recordJpaRepository.save(TestEntityFactory.createRecord(user,room));
        roomParticipantJpaRepository.save(TestEntityFactory.createRoomParticipant(room, user, RoomParticipantRole.HOST, 0.0));
    }

    @Test
    @DisplayName("사용자가 방 참여자이자 기록 작성자면, 기록을 핀할 수 있고 기록을 핀 하기 위한 책 정보가 응답된다")
    void pinRecord_success() throws Exception {
        // when
        mockMvc.perform(get("/rooms/{roomId}/records/{recordId}/pin", room.getRoomId(), record.getPostId())
                        .requestAttr("userId", user.getUserId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isbn").value(book.getIsbn()))
                .andExpect(jsonPath("$.data.bookImageUrl").value(book.getImageUrl()))
                .andExpect(jsonPath("$.data.bookTitle").value(book.getTitle()))
                .andExpect(jsonPath("$.data.authorName").value(book.getAuthorName())) ;
    }
}
