package konkuk.thip.roompost.adapter.in.web;

import com.jayway.jsonpath.JsonPath;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.domain.value.RoomParticipantRole;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.roomparticipant.RoomParticipantJpaRepository;
import konkuk.thip.room.domain.value.Category;
import konkuk.thip.roompost.adapter.out.jpa.AttendanceCheckJpaEntity;
import konkuk.thip.roompost.adapter.out.persistence.repository.attendancecheck.AttendanceCheckJpaRepository;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.domain.value.Alias;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static konkuk.thip.common.exception.code.ErrorCode.ROOM_ACCESS_FORBIDDEN;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[통합] 오늘의 한마디 조회 api 통합 테스트")
class AttendanceCheckShowApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private BookJpaRepository bookJpaRepository;
    @Autowired private RoomJpaRepository roomJpaRepository;
    @Autowired private RoomParticipantJpaRepository roomParticipantJpaRepository;
    @Autowired private AttendanceCheckJpaRepository attendanceCheckJpaRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("방의 출석체크(= 오늘의 한마디) 조회 요청하면, [오늘의 한마디 작성자 정보, 오늘의 한마디 정보] 등을 최신순으로 반환한다.")
    void attendance_check_show_test() throws Exception {
        //given
        Alias a0 = TestEntityFactory.createScienceAlias();
        UserJpaEntity me = userJpaRepository.save(TestEntityFactory.createUser(a0, "me"));
        UserJpaEntity user1 = userJpaRepository.save(TestEntityFactory.createUser(a0, "user1"));

        Category c0 = TestEntityFactory.createScienceCategory();
        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook());
        RoomJpaEntity room = roomJpaRepository.save(TestEntityFactory.createRoom(book, c0));

        // me, user1 이 room에 참여중인 상황
        roomParticipantJpaRepository.save(TestEntityFactory.createRoomParticipant(room, me, RoomParticipantRole.MEMBER, 0.0));
        roomParticipantJpaRepository.save(TestEntityFactory.createRoomParticipant(room, user1, RoomParticipantRole.MEMBER, 0.0));

        // me, user1가 room에 오늘의 한마디 작성함 (me : ac1, ac3 작성, user1 : ac2 작성)
        AttendanceCheckJpaEntity ac1 = attendanceCheckJpaRepository.save(TestEntityFactory.createAttendanceCheck("오한1", room, me));
        AttendanceCheckJpaEntity ac2 = attendanceCheckJpaRepository.save(TestEntityFactory.createAttendanceCheck("오한2", room, user1));
        AttendanceCheckJpaEntity ac3 = attendanceCheckJpaRepository.save(TestEntityFactory.createAttendanceCheck("오한3", room, me));

        // ac1 -> ac2 -> ac3 순으로 작성
        LocalDateTime base = LocalDateTime.now();
        jdbcTemplate.update(
                "UPDATE attendance_checks SET created_at = ? WHERE attendancecheck_id = ?",
                Timestamp.valueOf(base.minusMinutes(30)), ac1.getAttendanceCheckId());
        jdbcTemplate.update(
                "UPDATE attendance_checks SET created_at = ? WHERE attendancecheck_id = ?",
                Timestamp.valueOf(base.minusMinutes(20)), ac2.getAttendanceCheckId());
        jdbcTemplate.update(
                "UPDATE attendance_checks SET created_at = ? WHERE attendancecheck_id = ?",
                Timestamp.valueOf(base.minusMinutes(10)), ac3.getAttendanceCheckId());

        //when //then
        mockMvc.perform(get("/rooms/{roomId}/daily-greeting", room.getRoomId().intValue())
                        .requestAttr("userId", me.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.todayCommentList", hasSize(3)))
                // 정렬 순서 : 오늘의 한마디 작성 시각 기준 최신순
                .andExpect(jsonPath("$.data.todayCommentList[0].creatorId", is(me.getUserId().intValue())))
                .andExpect(jsonPath("$.data.todayCommentList[0].todayComment", is("오한3")))
                .andExpect(jsonPath("$.data.todayCommentList[1].creatorId", is(user1.getUserId().intValue())))
                .andExpect(jsonPath("$.data.todayCommentList[1].todayComment", is("오한2")))
                .andExpect(jsonPath("$.data.todayCommentList[2].creatorId", is(me.getUserId().intValue())))
                .andExpect(jsonPath("$.data.todayCommentList[2].todayComment", is("오한1")));
    }

    @Test
    @DisplayName("방의 멤버가 아닌 사람이 오늘의 한마디 조회 요청을 보낼 경우, 403 error가 발생한다.")
    void attendance_check_show_no_room_member() throws Exception {
        //given
        Alias a0 = TestEntityFactory.createScienceAlias();
        UserJpaEntity me = userJpaRepository.save(TestEntityFactory.createUser(a0, "me"));
        UserJpaEntity user1 = userJpaRepository.save(TestEntityFactory.createUser(a0, "user1"));

        Category c0 = TestEntityFactory.createScienceCategory();
        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook());
        RoomJpaEntity room = roomJpaRepository.save(TestEntityFactory.createRoom(book, c0));

        // user1 이 room에 참여중인 상황 (me는 아님)
        roomParticipantJpaRepository.save(TestEntityFactory.createRoomParticipant(room, user1, RoomParticipantRole.MEMBER, 0.0));

        // me, user1가 room에 오늘의 한마디 작성함 (me : ac1, ac3 작성, user1 : ac2 작성)
        AttendanceCheckJpaEntity ac2 = attendanceCheckJpaRepository.save(TestEntityFactory.createAttendanceCheck("오한2", room, user1));

        //when //then
        mockMvc.perform(get("/rooms/{roomId}/daily-greeting", room.getRoomId().intValue())
                        .requestAttr("userId", me.getUserId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString(ROOM_ACCESS_FORBIDDEN.getMessage())));
    }

    @Test
    @DisplayName("오늘의 한마디 조회는 작성 시각 기준 최신순 정렬 & 커서 기반 페이지네이션으로 동작한다.")
    void attendance_check_show_page_test() throws Exception {
        //given
        Alias a0 = TestEntityFactory.createScienceAlias();
        UserJpaEntity me = userJpaRepository.save(TestEntityFactory.createUser(a0, "me"));

        Category c0 = TestEntityFactory.createScienceCategory();
        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook());
        RoomJpaEntity room = roomJpaRepository.save(TestEntityFactory.createRoom(book, c0));

        // user1 이 room에 참여중인 상황 (me는 아님)
        roomParticipantJpaRepository.save(TestEntityFactory.createRoomParticipant(room, me, RoomParticipantRole.MEMBER, 0.0));

        // me가 room에 오늘의 한마디 작성함
        AttendanceCheckJpaEntity ac1 = attendanceCheckJpaRepository.save(TestEntityFactory.createAttendanceCheck("오한1", room, me));
        AttendanceCheckJpaEntity ac2 = attendanceCheckJpaRepository.save(TestEntityFactory.createAttendanceCheck("오한2", room, me));
        AttendanceCheckJpaEntity ac3 = attendanceCheckJpaRepository.save(TestEntityFactory.createAttendanceCheck("오한3", room, me));
        AttendanceCheckJpaEntity ac4 = attendanceCheckJpaRepository.save(TestEntityFactory.createAttendanceCheck("오한4", room, me));
        AttendanceCheckJpaEntity ac5 = attendanceCheckJpaRepository.save(TestEntityFactory.createAttendanceCheck("오한5", room, me));
        AttendanceCheckJpaEntity ac6 = attendanceCheckJpaRepository.save(TestEntityFactory.createAttendanceCheck("오한6", room, me));
        AttendanceCheckJpaEntity ac7 = attendanceCheckJpaRepository.save(TestEntityFactory.createAttendanceCheck("오한7", room, me));
        AttendanceCheckJpaEntity ac8 = attendanceCheckJpaRepository.save(TestEntityFactory.createAttendanceCheck("오한8", room, me));
        AttendanceCheckJpaEntity ac9 = attendanceCheckJpaRepository.save(TestEntityFactory.createAttendanceCheck("오한9", room, me));
        AttendanceCheckJpaEntity ac10 = attendanceCheckJpaRepository.save(TestEntityFactory.createAttendanceCheck("오한10", room, me));
        AttendanceCheckJpaEntity ac11 = attendanceCheckJpaRepository.save(TestEntityFactory.createAttendanceCheck("오한11", room, me));
        AttendanceCheckJpaEntity ac12 = attendanceCheckJpaRepository.save(TestEntityFactory.createAttendanceCheck("오한12", room, me));

        // ac1 -> ac2 -> ,,, -> ac12 순으로 작성
        LocalDateTime base = LocalDateTime.now();
        jdbcTemplate.update(
                "UPDATE attendance_checks SET created_at = ? WHERE attendancecheck_id = ?",
                Timestamp.valueOf(base.minusMinutes(60)), ac1.getAttendanceCheckId());
        jdbcTemplate.update(
                "UPDATE attendance_checks SET created_at = ? WHERE attendancecheck_id = ?",
                Timestamp.valueOf(base.minusMinutes(55)), ac2.getAttendanceCheckId());
        jdbcTemplate.update(
                "UPDATE attendance_checks SET created_at = ? WHERE attendancecheck_id = ?",
                Timestamp.valueOf(base.minusMinutes(50)), ac3.getAttendanceCheckId());
        jdbcTemplate.update(
                "UPDATE attendance_checks SET created_at = ? WHERE attendancecheck_id = ?",
                Timestamp.valueOf(base.minusMinutes(45)), ac4.getAttendanceCheckId());
        jdbcTemplate.update(
                "UPDATE attendance_checks SET created_at = ? WHERE attendancecheck_id = ?",
                Timestamp.valueOf(base.minusMinutes(40)), ac5.getAttendanceCheckId());
        jdbcTemplate.update(
                "UPDATE attendance_checks SET created_at = ? WHERE attendancecheck_id = ?",
                Timestamp.valueOf(base.minusMinutes(35)), ac6.getAttendanceCheckId());
        jdbcTemplate.update(
                "UPDATE attendance_checks SET created_at = ? WHERE attendancecheck_id = ?",
                Timestamp.valueOf(base.minusMinutes(30)), ac7.getAttendanceCheckId());
        jdbcTemplate.update(
                "UPDATE attendance_checks SET created_at = ? WHERE attendancecheck_id = ?",
                Timestamp.valueOf(base.minusMinutes(25)), ac8.getAttendanceCheckId());
        jdbcTemplate.update(
                "UPDATE attendance_checks SET created_at = ? WHERE attendancecheck_id = ?",
                Timestamp.valueOf(base.minusMinutes(20)), ac9.getAttendanceCheckId());
        jdbcTemplate.update(
                "UPDATE attendance_checks SET created_at = ? WHERE attendancecheck_id = ?",
                Timestamp.valueOf(base.minusMinutes(15)), ac10.getAttendanceCheckId());
        jdbcTemplate.update(
                "UPDATE attendance_checks SET created_at = ? WHERE attendancecheck_id = ?",
                Timestamp.valueOf(base.minusMinutes(10)), ac11.getAttendanceCheckId());
        jdbcTemplate.update(
                "UPDATE attendance_checks SET created_at = ? WHERE attendancecheck_id = ?",
                Timestamp.valueOf(base.minusMinutes(5)), ac12.getAttendanceCheckId());

        //when //then
        MvcResult firstResult = mockMvc.perform(get("/rooms/{roomId}/daily-greeting", room.getRoomId().intValue())
                        .requestAttr("userId", me.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.todayCommentList", hasSize(10)))
                // 정렬 순서 : 오늘의 한마디 작성 시각 기준 최신순
                .andExpect(jsonPath("$.data.todayCommentList[0].todayComment", is("오한12")))
                .andExpect(jsonPath("$.data.todayCommentList[1].todayComment", is("오한11")))
                .andExpect(jsonPath("$.data.todayCommentList[2].todayComment", is("오한10")))
                .andExpect(jsonPath("$.data.todayCommentList[3].todayComment", is("오한9")))
                .andExpect(jsonPath("$.data.todayCommentList[4].todayComment", is("오한8")))
                .andExpect(jsonPath("$.data.todayCommentList[5].todayComment", is("오한7")))
                .andExpect(jsonPath("$.data.todayCommentList[6].todayComment", is("오한6")))
                .andExpect(jsonPath("$.data.todayCommentList[7].todayComment", is("오한5")))
                .andExpect(jsonPath("$.data.todayCommentList[8].todayComment", is("오한4")))
                .andExpect(jsonPath("$.data.todayCommentList[9].todayComment", is("오한3")))
                .andReturn();

        String responseBody = firstResult.getResponse().getContentAsString();
        String nextCursor = JsonPath.read(responseBody, "$.data.nextCursor");

        mockMvc.perform(get("/rooms/{roomId}/daily-greeting", room.getRoomId().intValue())
                        .requestAttr("userId", me.getUserId())
                        .param("cursor", nextCursor))       // 2페이지 요청
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.todayCommentList", hasSize(2)))
                // 정렬 순서 : 오늘의 한마디 작성 시각 기준 최신순
                .andExpect(jsonPath("$.data.todayCommentList[0].todayComment", is("오한2")))
                .andExpect(jsonPath("$.data.todayCommentList[1].todayComment", is("오한1")));
    }
}
