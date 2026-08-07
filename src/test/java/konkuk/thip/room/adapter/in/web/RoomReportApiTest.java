package konkuk.thip.room.adapter.in.web;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.roomparticipant.RoomParticipantJpaRepository;
import konkuk.thip.room.domain.value.Category;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.domain.value.Alias;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static konkuk.thip.common.exception.code.ErrorCode.ROOM_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[통합] 모임방 신고 api 통합 테스트")
class RoomReportApiTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private BookJpaRepository bookJpaRepository;
    @Autowired private RoomJpaRepository roomJpaRepository;
    @Autowired private RoomParticipantJpaRepository roomParticipantJpaRepository;

    private UserJpaEntity user;
    private RoomJpaEntity room;

    private static final String ROOM_REPORT_API_PATH = "/rooms/{roomId}/report";

    @BeforeEach
    void setUp() {
        Alias alias = TestEntityFactory.createLiteratureAlias();
        user = userJpaRepository.save(TestEntityFactory.createUser(alias));
        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBookWithISBN("9788954682152"));
        Category category = TestEntityFactory.createLiteratureCategory();
        room = roomJpaRepository.save(TestEntityFactory.createRoom(book, category));
    }

    @AfterEach
    void tearDown() {
        roomParticipantJpaRepository.deleteAllInBatch();
        roomJpaRepository.deleteAllInBatch();
        bookJpaRepository.deleteAllInBatch();
        userJpaRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("모임방을 신고하면 신고 횟수만 1 증가한다 [성공]")
    void reportRoom_success() throws Exception {
        // when
        mockMvc.perform(post(ROOM_REPORT_API_PATH, room.getRoomId())
                        .requestAttr("userId", user.getUserId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roomId").value(room.getRoomId()))
                .andExpect(jsonPath("$.data.reportCount").value(1));

        // then
        RoomJpaEntity updatedRoom = roomJpaRepository.findById(room.getRoomId()).orElseThrow();
        assertThat(updatedRoom.getReportCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("같은 모임방을 두 번 신고하면 신고 횟수가 누적된다 [성공]")
    void reportRoom_twice_accumulates() throws Exception {
        // when
        mockMvc.perform(post(ROOM_REPORT_API_PATH, room.getRoomId())
                        .requestAttr("userId", user.getUserId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(post(ROOM_REPORT_API_PATH, room.getRoomId())
                        .requestAttr("userId", user.getUserId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reportCount").value(2));

        // then
        RoomJpaEntity updatedRoom = roomJpaRepository.findById(room.getRoomId()).orElseThrow();
        assertThat(updatedRoom.getReportCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("존재하지 않는 모임방을 신고하면 [404 에러 발생]")
    void reportRoom_notFound_fail() throws Exception {
        // given
        long nonExistentRoomId = 999_999L;

        // when & then
        mockMvc.perform(post(ROOM_REPORT_API_PATH, nonExistentRoomId)
                        .requestAttr("userId", user.getUserId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ROOM_NOT_FOUND.getCode()));
    }
}
