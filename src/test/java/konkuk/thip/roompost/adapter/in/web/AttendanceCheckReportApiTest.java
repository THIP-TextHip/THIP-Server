package konkuk.thip.roompost.adapter.in.web;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.notification.adapter.out.persistence.repository.NotificationJpaRepository;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.roomparticipant.RoomParticipantJpaRepository;
import konkuk.thip.room.domain.value.Category;
import konkuk.thip.roompost.adapter.out.jpa.AttendanceCheckJpaEntity;
import konkuk.thip.roompost.adapter.out.persistence.repository.attendancecheck.AttendanceCheckJpaRepository;
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

import static konkuk.thip.common.exception.code.ErrorCode.ATTENDANCE_CHECK_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[통합] 오늘의 한마디 신고 api 통합 테스트")
class AttendanceCheckReportApiTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private BookJpaRepository bookJpaRepository;
    @Autowired private RoomJpaRepository roomJpaRepository;
    @Autowired private RoomParticipantJpaRepository roomParticipantJpaRepository;
    @Autowired private AttendanceCheckJpaRepository attendanceCheckJpaRepository;
    @Autowired private NotificationJpaRepository notificationJpaRepository;

    private UserJpaEntity user;
    private RoomJpaEntity room;
    private AttendanceCheckJpaEntity attendanceCheck;

    private static final String ATTENDANCE_CHECK_REPORT_API_PATH = "/rooms/{roomId}/daily-greeting/{attendanceCheckId}/report";

    @BeforeEach
    void setUp() {
        Alias alias = TestEntityFactory.createLiteratureAlias();
        user = userJpaRepository.save(TestEntityFactory.createUser(alias));
        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBookWithISBN("9788954682152"));
        Category category = TestEntityFactory.createLiteratureCategory();
        room = roomJpaRepository.save(TestEntityFactory.createRoom(book, category));
        attendanceCheck = attendanceCheckJpaRepository.save(
                TestEntityFactory.createAttendanceCheck("오늘의 한마디", room, user)
        );
    }

    @AfterEach
    void tearDown() {
        attendanceCheckJpaRepository.deleteAllInBatch();
        roomParticipantJpaRepository.deleteAllInBatch();
        roomJpaRepository.deleteAllInBatch();
        bookJpaRepository.deleteAllInBatch();
        // 다른 테스트에서 생성된 알림이 정리되지 않은 채 남아 있으면 유저 삭제 시 FK 위반이 발생하므로 함께 정리
        notificationJpaRepository.deleteAllInBatch();
        userJpaRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("오늘의 한마디를 신고하면 신고 횟수만 1 증가한다 [성공]")
    void reportAttendanceCheck_success() throws Exception {
        // when
        mockMvc.perform(post(ATTENDANCE_CHECK_REPORT_API_PATH, room.getRoomId(), attendanceCheck.getAttendanceCheckId())
                        .requestAttr("userId", user.getUserId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.attendanceCheckId").value(attendanceCheck.getAttendanceCheckId()))
                .andExpect(jsonPath("$.data.reportCount").value(1));

        // then
        AttendanceCheckJpaEntity updated = attendanceCheckJpaRepository.findByAttendanceCheckId(attendanceCheck.getAttendanceCheckId()).orElseThrow();
        assertThat(updated.getReportCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("같은 오늘의 한마디를 두 번 신고하면 신고 횟수가 누적된다 [성공]")
    void reportAttendanceCheck_twice_accumulates() throws Exception {
        // when
        mockMvc.perform(post(ATTENDANCE_CHECK_REPORT_API_PATH, room.getRoomId(), attendanceCheck.getAttendanceCheckId())
                        .requestAttr("userId", user.getUserId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(post(ATTENDANCE_CHECK_REPORT_API_PATH, room.getRoomId(), attendanceCheck.getAttendanceCheckId())
                        .requestAttr("userId", user.getUserId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reportCount").value(2));

        // then
        AttendanceCheckJpaEntity updated = attendanceCheckJpaRepository.findByAttendanceCheckId(attendanceCheck.getAttendanceCheckId()).orElseThrow();
        assertThat(updated.getReportCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("존재하지 않는 오늘의 한마디를 신고하면 [404 에러 발생]")
    void reportAttendanceCheck_notFound_fail() throws Exception {
        // given
        long nonExistentAttendanceCheckId = 999_999L;

        // when & then
        mockMvc.perform(post(ATTENDANCE_CHECK_REPORT_API_PATH, room.getRoomId(), nonExistentAttendanceCheckId)
                        .requestAttr("userId", user.getUserId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ATTENDANCE_CHECK_NOT_FOUND.getCode()));
    }
}
