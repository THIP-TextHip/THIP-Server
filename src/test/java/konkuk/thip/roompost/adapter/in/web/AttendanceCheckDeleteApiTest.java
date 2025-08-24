package konkuk.thip.roompost.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomParticipantRole;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.roomparticipant.RoomParticipantJpaRepository;
import konkuk.thip.room.domain.value.Category;
import konkuk.thip.roompost.adapter.out.jpa.AttendanceCheckJpaEntity;
import konkuk.thip.roompost.adapter.out.persistence.repository.attendancecheck.AttendanceCheckJpaRepository;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.domain.value.Alias;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static konkuk.thip.common.entity.StatusType.INACTIVE;
import static konkuk.thip.common.exception.code.ErrorCode.ATTENDANCE_CHECK_CAN_NOT_DELETE;
import static konkuk.thip.common.exception.code.ErrorCode.ROOM_ACCESS_FORBIDDEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[통합] 오늘의 한마디 삭제 api 통합 테스트")
class AttendanceCheckDeleteApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private BookJpaRepository bookJpaRepository;
    @Autowired private RoomJpaRepository roomJpaRepository;
    @Autowired private RoomParticipantJpaRepository roomParticipantJpaRepository;
    @Autowired private AttendanceCheckJpaRepository attendanceCheckJpaRepository;

    @AfterEach
    void tearDown() {
        attendanceCheckJpaRepository.deleteAllInBatch();
        roomParticipantJpaRepository.deleteAllInBatch();
        roomJpaRepository.deleteAllInBatch();
        bookJpaRepository.deleteAllInBatch();
        userJpaRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("오늘의 한마디 작성자는 본인이 작성한 오늘의 한마디를 삭제(= soft delete) 할 수 있다.")
    void attendance_check_delete_test() throws Exception {
        //given
        Alias a0 = TestEntityFactory.createScienceAlias();
        UserJpaEntity me = userJpaRepository.save(TestEntityFactory.createUser(a0, "me"));

        Category c0 = TestEntityFactory.createScienceCategory();
        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook());
        RoomJpaEntity room = roomJpaRepository.save(TestEntityFactory.createRoom(book, c0));

        // me 가 room에 참여중인 상황
        roomParticipantJpaRepository.save(TestEntityFactory.createRoomParticipant(room, me, RoomParticipantRole.MEMBER, 0.0));

        // me가 오늘의 한마디 작성한 상황
        AttendanceCheckJpaEntity ac1 = attendanceCheckJpaRepository.save(TestEntityFactory.createAttendanceCheck("오한1", room, me));

        //when //then
        mockMvc.perform(delete("/rooms/{roomId}/daily-greeting/{attendanceCheckId}", room.getRoomId().intValue(), ac1.getAttendanceCheckId().intValue())
                        .requestAttr("userId", me.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roomId", is(room.getRoomId().intValue())));

        AttendanceCheckJpaEntity deleted = attendanceCheckJpaRepository.findById(ac1.getAttendanceCheckId()).orElse(null);
        Assertions.assertNotNull(deleted);
        assertThat(deleted.getStatus()).isEqualTo(INACTIVE);
    }

    @Test
    @DisplayName("방 참여자가 아닌 사람이 오늘의 한마디 삭제 요청을 할 경우, 403 error 를 반환한다.")
    void attendance_check_delete_not_room_participant_test() throws Exception {
        //given
        Alias a0 = TestEntityFactory.createScienceAlias();
        UserJpaEntity me = userJpaRepository.save(TestEntityFactory.createUser(a0, "me"));
        UserJpaEntity user1 = userJpaRepository.save(TestEntityFactory.createUser(a0, "user1"));

        Category c0 = TestEntityFactory.createScienceCategory();
        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook());
        RoomJpaEntity room = roomJpaRepository.save(TestEntityFactory.createRoom(book, c0));

        // me 가 room에 참여중인 상황 (user1은 참여 안함)
        roomParticipantJpaRepository.save(TestEntityFactory.createRoomParticipant(room, me, RoomParticipantRole.MEMBER, 0.0));

        // me가 오늘의 한마디 작성한 상황
        AttendanceCheckJpaEntity ac1 = attendanceCheckJpaRepository.save(TestEntityFactory.createAttendanceCheck("오한1", room, me));

        //when //then
        mockMvc.perform(delete("/rooms/{roomId}/daily-greeting/{attendanceCheckId}", room.getRoomId().intValue(), ac1.getAttendanceCheckId().intValue())
                        .requestAttr("userId", user1.getUserId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString(ROOM_ACCESS_FORBIDDEN.getMessage())));
    }

    @Test
    @DisplayName("다른 사람이 작성한 오늘의 한마디의 삭제 요청을 할 경우, 400 error 를 반환한다.")
    void attendance_check_delete_not_creator_test() throws Exception {
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

        // me, user1 이 오늘의 한마디 작성한 상황
        AttendanceCheckJpaEntity ac1 = attendanceCheckJpaRepository.save(TestEntityFactory.createAttendanceCheck("me-오한1", room, me));
        AttendanceCheckJpaEntity ac2 = attendanceCheckJpaRepository.save(TestEntityFactory.createAttendanceCheck("user1-오한1", room, user1));

        //when //then : me가 user1이 작성한 오늘의 한마디를 삭제 요청
        mockMvc.perform(delete("/rooms/{roomId}/daily-greeting/{attendanceCheckId}", room.getRoomId().intValue(), ac2.getAttendanceCheckId().intValue())
                        .requestAttr("userId", me.getUserId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString(ATTENDANCE_CHECK_CAN_NOT_DELETE.getMessage())));
    }
}
