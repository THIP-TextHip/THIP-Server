package konkuk.thip.user.adapter.in.web;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.roomparticipant.RoomParticipantJpaRepository;
import konkuk.thip.room.domain.value.Category;
import konkuk.thip.room.domain.value.RoomParticipantRole;
import konkuk.thip.roompost.adapter.out.persistence.repository.attendancecheck.AttendanceCheckJpaRepository;
import konkuk.thip.roompost.adapter.out.persistence.repository.record.RecordJpaRepository;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.adapter.out.persistence.repository.block.UserBlockJpaRepository;
import konkuk.thip.user.domain.value.Alias;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[통합] 차단 후 모임방 콘텐츠 노출 차단 검증")
class BlockRoomContentHiddenApiTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private UserBlockJpaRepository userBlockJpaRepository;
    @Autowired private BookJpaRepository bookJpaRepository;
    @Autowired private RoomJpaRepository roomJpaRepository;
    @Autowired private RoomParticipantJpaRepository roomParticipantJpaRepository;
    @Autowired private RecordJpaRepository recordJpaRepository;
    @Autowired private AttendanceCheckJpaRepository attendanceCheckJpaRepository;

    private UserJpaEntity viewer;
    private UserJpaEntity blocked;
    private RoomJpaEntity room;

    @BeforeEach
    void setUp() {
        Alias alias = TestEntityFactory.createLiteratureAlias();
        viewer = userJpaRepository.save(TestEntityFactory.createUser(alias, "viewer"));
        blocked = userJpaRepository.save(TestEntityFactory.createUser(alias, "blockedUser"));

        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBookWithISBN("9788954682152"));
        Category category = TestEntityFactory.createLiteratureCategory();
        room = roomJpaRepository.save(TestEntityFactory.createRoom(book, category));

        // 둘 다 같은 방에 참여 중 (viewer 가 방장)
        roomParticipantJpaRepository.save(
                TestEntityFactory.createRoomParticipant(room, viewer, RoomParticipantRole.HOST, 0.0));
        roomParticipantJpaRepository.save(
                TestEntityFactory.createRoomParticipant(room, blocked, RoomParticipantRole.MEMBER, 0.0));
    }

    @AfterEach
    void tearDown() {
        userBlockJpaRepository.deleteAllInBatch();
        attendanceCheckJpaRepository.deleteAllInBatch();
        recordJpaRepository.deleteAllInBatch();
        roomParticipantJpaRepository.deleteAllInBatch();
        roomJpaRepository.deleteAllInBatch();
        bookJpaRepository.deleteAllInBatch();
        userJpaRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("[성공] 차단하면 그룹 기록 목록에서 그 사용자의 기록이 사라진다.")
    void blockedUserRecord_disappears_from_group_records() throws Exception {
        // given
        recordJpaRepository.save(TestEntityFactory.createRecord(blocked, room));

        mockMvc.perform(get("/rooms/{roomId}/posts", room.getRoomId())
                        .param("type", "group")
                        .param("sort", "latest")
                        .requestAttr("userId", viewer.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.postList", hasSize(1)));

        // when
        block(viewer, blocked);

        // then
        mockMvc.perform(get("/rooms/{roomId}/posts", room.getRoomId())
                        .param("type", "group")
                        .param("sort", "latest")
                        .requestAttr("userId", viewer.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.postList", hasSize(0)));
    }

    @Test
    @DisplayName("[성공] 차단하면 오늘의 한마디 목록에서 그 사용자의 글이 사라진다.")
    void blockedUserAttendanceCheck_disappears() throws Exception {
        // given
        attendanceCheckJpaRepository.save(
                TestEntityFactory.createAttendanceCheck("오늘의 한마디", room, blocked));

        mockMvc.perform(get("/rooms/{roomId}/daily-greeting", room.getRoomId())
                        .requestAttr("userId", viewer.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.todayCommentList", hasSize(1)));

        // when
        block(viewer, blocked);

        // then
        mockMvc.perform(get("/rooms/{roomId}/daily-greeting", room.getRoomId())
                        .requestAttr("userId", viewer.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.todayCommentList", hasSize(0)));
    }

    @Test
    @DisplayName("[성공] 차단하면 모임방 멤버 목록에서 그 사용자가 사라진다. (이미 참여 중인 방 자체는 유지)")
    void blockedUser_disappears_from_member_list() throws Exception {
        // given : 차단 전에는 두 명 모두 보인다
        mockMvc.perform(get("/rooms/{roomId}/users", room.getRoomId())
                        .requestAttr("userId", viewer.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userList", hasSize(2)));

        // when
        block(viewer, blocked);

        // then : 차단 사용자만 빠지고 방은 그대로 조회된다
        mockMvc.perform(get("/rooms/{roomId}/users", room.getRoomId())
                        .requestAttr("userId", viewer.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userList", hasSize(1)))
                .andExpect(jsonPath("$.data.userList[0].userId").value(viewer.getUserId()));
    }

    private void block(UserJpaEntity blocker, UserJpaEntity target) {
        userBlockJpaRepository.save(TestEntityFactory.createUserBlock(blocker, target));
        userBlockJpaRepository.flush();
    }
}
