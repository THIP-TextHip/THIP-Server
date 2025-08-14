package konkuk.thip.room.adapter.in.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import konkuk.thip.room.adapter.out.persistence.repository.attendancecheck.AttendanceCheckJpaRepository;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Map;

import static konkuk.thip.common.exception.code.ErrorCode.ATTENDANCE_CHECK_WRITE_LIMIT_EXCEEDED;
import static konkuk.thip.common.exception.code.ErrorCode.ROOM_ACCESS_FORBIDDEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[통합] 오늘의 한마디 생성 api 통합 테스트")
class AttendanceCheckCreateApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;
    @Autowired private AliasJpaRepository aliasJpaRepository;
    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private CategoryJpaRepository categoryJpaRepository;
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
        categoryJpaRepository.deleteAllInBatch();
        userJpaRepository.deleteAllInBatch();
        aliasJpaRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("방의 참석자는 출석체크(= 오늘의 한마디) 를 작성할 수 있다.")
    void attendance_check_create_test() throws Exception {
        //given
        AliasJpaEntity a0 = aliasJpaRepository.save(TestEntityFactory.createScienceAlias());
        UserJpaEntity me = userJpaRepository.save(TestEntityFactory.createUser(a0, "me"));

        CategoryJpaEntity c0 = categoryJpaRepository.save(TestEntityFactory.createScienceCategory(a0));
        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook());
        RoomJpaEntity room = roomJpaRepository.save(TestEntityFactory.createRoom(book, c0));

        // me 가 room에 참여중인 상황
        roomParticipantJpaRepository.save(TestEntityFactory.createRoomParticipant(room, me, RoomParticipantRole.MEMBER, 0.0));

        Map<String, Object> request = Map.of("content", "오늘의 한마디~~~");

        //when //then
        ResultActions result = mockMvc.perform(post("/rooms/{roomId}/daily-greeting", room.getRoomId().intValue())
                        .requestAttr("userId", me.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        Long savedId = attendanceCheckJpaRepository.findAll().get(0).getAttendanceCheckId();        // 저장된 출석 체크 id값

        String responseBody = result.andReturn().getResponse().getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseBody);

        assertThat(responseJson.path("data").path("attendanceCheckId").asLong()).isEqualTo(savedId);
    }

    @Test
    @DisplayName("방의 참석자가 아닐 경우, 오늘의 한마디를 작성 요청 시 403 에러 발생한다(400 에러 아님).")
    void attendance_check_fail_test() throws Exception {
        //given : me 는 room에 속하지 않는 유저
        AliasJpaEntity a0 = aliasJpaRepository.save(TestEntityFactory.createScienceAlias());
        UserJpaEntity me = userJpaRepository.save(TestEntityFactory.createUser(a0, "me"));

        CategoryJpaEntity c0 = categoryJpaRepository.save(TestEntityFactory.createScienceCategory(a0));
        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook());
        RoomJpaEntity room = roomJpaRepository.save(TestEntityFactory.createRoom(book, c0));

        Map<String, Object> request = Map.of("content", "오늘의 한마디~~~");
        //when //then
        mockMvc.perform(post("/rooms/{roomId}/daily-greeting", room.getRoomId().intValue())
                        .requestAttr("userId", me.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ROOM_ACCESS_FORBIDDEN.getCode()))
                .andExpect(jsonPath("$.message", containsString(ROOM_ACCESS_FORBIDDEN.getMessage())));
    }

    @Test
    @DisplayName("방의 참석자가 출석체크(= 오늘의 한마디) 를 처음 작성할 경우, response의 isFirstWrite는 true 이다.")
    void attendance_check_create_first_test() throws Exception {
        //given
        AliasJpaEntity a0 = aliasJpaRepository.save(TestEntityFactory.createScienceAlias());
        UserJpaEntity me = userJpaRepository.save(TestEntityFactory.createUser(a0, "me"));

        CategoryJpaEntity c0 = categoryJpaRepository.save(TestEntityFactory.createScienceCategory(a0));
        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook());
        RoomJpaEntity room = roomJpaRepository.save(TestEntityFactory.createRoom(book, c0));

        // me 가 room에 참여중인 상황
        roomParticipantJpaRepository.save(TestEntityFactory.createRoomParticipant(room, me, RoomParticipantRole.MEMBER, 0.0));

        Map<String, Object> request = Map.of("content", "오늘의 한마디~~~");

        //when //then
        ResultActions result = mockMvc.perform(post("/rooms/{roomId}/daily-greeting", room.getRoomId().intValue())
                        .requestAttr("userId", me.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        String responseBody = result.andReturn().getResponse().getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseBody);

        assertThat(responseJson.path("data").path("isFirstWrite").asBoolean()).isTrue();
    }

    @Test
    @DisplayName("방의 참석자가 출석체크(= 오늘의 한마디) 를 하루 최대 5회만 작성할 수 있다. 5회를 초과할 경우, 400 error를 반환한다.")
    void attendance_check_create_too_many_test() throws Exception {
        //given
        AliasJpaEntity a0 = aliasJpaRepository.save(TestEntityFactory.createScienceAlias());
        UserJpaEntity me = userJpaRepository.save(TestEntityFactory.createUser(a0, "me"));

        CategoryJpaEntity c0 = categoryJpaRepository.save(TestEntityFactory.createScienceCategory(a0));
        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook());
        RoomJpaEntity room = roomJpaRepository.save(TestEntityFactory.createRoom(book, c0));

        // me 가 room에 참여중인 상황
        roomParticipantJpaRepository.save(TestEntityFactory.createRoomParticipant(room, me, RoomParticipantRole.MEMBER, 0.0));

        // me 가 이미 오늘의 한마디를 5회 작성한 상황
        attendanceCheckJpaRepository.save(TestEntityFactory.createAttendanceCheck("오한1", room, me));
        attendanceCheckJpaRepository.save(TestEntityFactory.createAttendanceCheck("오한2", room, me));
        attendanceCheckJpaRepository.save(TestEntityFactory.createAttendanceCheck("오한3", room, me));
        attendanceCheckJpaRepository.save(TestEntityFactory.createAttendanceCheck("오한4", room, me));
        attendanceCheckJpaRepository.save(TestEntityFactory.createAttendanceCheck("오한5", room, me));

        Map<String, Object> request = Map.of("content", "6번째 오늘의 한마디~~~");

        //when //then
        mockMvc.perform(post("/rooms/{roomId}/daily-greeting", room.getRoomId().intValue())
                        .requestAttr("userId", me.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ATTENDANCE_CHECK_WRITE_LIMIT_EXCEEDED.getCode()))
                .andExpect(jsonPath("$.message", containsString(ATTENDANCE_CHECK_WRITE_LIMIT_EXCEEDED.getMessage())));
    }
}
