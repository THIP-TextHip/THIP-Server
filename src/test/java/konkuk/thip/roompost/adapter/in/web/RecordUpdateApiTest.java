package konkuk.thip.roompost.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomParticipantJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomParticipantRole;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.roomparticipant.RoomParticipantJpaRepository;
import konkuk.thip.room.domain.value.Category;
import konkuk.thip.roompost.adapter.out.jpa.RecordJpaEntity;
import konkuk.thip.roompost.adapter.out.persistence.repository.record.RecordJpaRepository;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.domain.value.Alias;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static konkuk.thip.common.exception.code.ErrorCode.API_INVALID_PARAM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[통합] 기록 수정 api 통합 테스트")
class RecordUpdateApiTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Autowired UserJpaRepository userJpaRepository;
    @Autowired BookJpaRepository bookJpaRepository;
    @Autowired RoomJpaRepository roomJpaRepository;
    @Autowired RoomParticipantJpaRepository roomParticipantJpaRepository;
    @Autowired RecordJpaRepository recordJpaRepository;

    private UserJpaEntity author;          // 기록 작성자(방 참가자)
    private UserJpaEntity otherMember;     // 작성자가 아닌 다른 참가자
    private UserJpaEntity outsider;        // 방 참가자가 아닌 사용자
    private RoomJpaEntity room;
    private RecordJpaEntity record;

    @BeforeEach
    void setUp() {
        // 1) 사용자 3명
        author = userJpaRepository.save(TestEntityFactory.createUser(Alias.WRITER, "작성자"));
        otherMember = userJpaRepository.save(TestEntityFactory.createUser(Alias.WRITER, "다른참가자"));
        outsider = userJpaRepository.save(TestEntityFactory.createUser(Alias.WRITER, "외부인"));

        // 2) 도서/방
        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook());
        Category category = TestEntityFactory.createLiteratureCategory();
        room = roomJpaRepository.save(TestEntityFactory.createRoom(book, category));

        // 3) 방 참가자(작성자, 다른참가자만 방에 소속)
        roomParticipantJpaRepository.save(
                RoomParticipantJpaEntity.builder()
                        .currentPage(10)
                        .userPercentage(80.0)
                        .roomParticipantRole(RoomParticipantRole.HOST)
                        .userJpaEntity(author)
                        .roomJpaEntity(room)
                        .build()
        );
        roomParticipantJpaRepository.save(
                RoomParticipantJpaEntity.builder()
                        .currentPage(5)
                        .userPercentage(50.0)
                        .roomParticipantRole(RoomParticipantRole.MEMBER)
                        .userJpaEntity(otherMember)
                        .roomJpaEntity(room)
                        .build()
        );

        // 4) 기존 기록(작성자가 생성한 기록)
        record = recordJpaRepository.save(TestEntityFactory.createRecord(author, room));
    }

    @Test
    @DisplayName("[성공] 작성자이자 방 참가자가 내용을 수정하면 200 OK, DB 반영")
    void update_record_success() throws Exception {
        // given
        String newContent = "수정된 기록 내용";
        Map<String, Object> body = Map.of("content", newContent);

        // when & then
        mockMvc.perform(patch("/rooms/{roomId}/records/{recordId}", room.getRoomId(), record.getPostId())
                        .requestAttr("userId", author.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        // DB 반영 확인
        RecordJpaEntity updated = recordJpaRepository.findById(record.getPostId()).orElseThrow();
        assertThat(updated.getContent()).isEqualTo(newContent);
    }

    @Test
    @DisplayName("[실패-검증] content가 공백이면 400 Bad Request")
    void update_record_validation_blank() throws Exception {
        // given
        Map<String, Object> body = Map.of("content", "");

        // when & then
        mockMvc.perform(patch("/rooms/{roomId}/records/{recordId}", room.getRoomId(), record.getPostId())
                        .requestAttr("userId", author.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(API_INVALID_PARAM.getCode()))
                .andExpect(jsonPath("$.message").exists()); // 상세 문구는 구현체 메시지에 의존
    }

    @Test
    @DisplayName("[실패-검증] content가 500자 초과면 400 Bad Request")
    void update_record_validation_too_long() throws Exception {
        // given
        String tooLong = "가".repeat(501);
        Map<String, Object> body = Map.of("content", tooLong);

        // when & then
        mockMvc.perform(patch("/rooms/{roomId}/records/{recordId}", room.getRoomId(), record.getPostId())
                        .requestAttr("userId", author.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(API_INVALID_PARAM.getCode()))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("[실패-권한] 방 참가자가 아니면 403 Forbidden, DB 불변")
    void update_record_forbidden_not_room_member() throws Exception {
        // given
        String prev = record.getContent();
        Map<String, Object> body = Map.of("content", "외부인이 수정 시도");

        // when & then
        mockMvc.perform(patch("/rooms/{roomId}/records/{recordId}", room.getRoomId(), record.getPostId())
                        .requestAttr("userId", outsider.getUserId()) // 참가자 아님
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());

        // DB 불변 확인
        RecordJpaEntity after = recordJpaRepository.findById(record.getPostId()).orElseThrow();
        assertThat(after.getContent()).isEqualTo(prev);
    }

    @Test
    @DisplayName("[실패-권한] 작성자가 아니면 403 Forbidden, DB 불변")
    void update_record_forbidden_not_creator() throws Exception {
        // given
        String prev = record.getContent();
        Map<String, Object> body = Map.of("content", "작성자가 아닌 회원이 수정 시도");

        // when & then
        mockMvc.perform(patch("/rooms/{roomId}/records/{recordId}", room.getRoomId(), record.getPostId())
                        .requestAttr("userId", otherMember.getUserId()) // 참가자지만 작성자 아님
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());

        // DB 불변 확인
        RecordJpaEntity after = recordJpaRepository.findById(record.getPostId()).orElseThrow();
        assertThat(after.getContent()).isEqualTo(prev);
    }

    @Test
    @DisplayName("[실패-존재X] 없는 recordId면 404 Not Found")
    void update_record_not_found() throws Exception {
        // given
        Map<String, Object> body = Map.of("content", "아무 내용");
        Long notExistId = Long.MAX_VALUE;

        // when & then
        mockMvc.perform(patch("/rooms/{roomId}/records/{recordId}", room.getRoomId(), notExistId)
                        .requestAttr("userId", author.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Nested
    @DisplayName("[실패-무결성] 잘못된 roomId로 요청 시 접근 거부(해당 방 소속 기록이 아님)")
    class WrongRoomIdCases {

        private RoomJpaEntity otherRoom;

        @BeforeEach
        void setUpOtherRoom() {
            BookJpaEntity book2 = bookJpaRepository.save(TestEntityFactory.createBook());
            otherRoom = roomJpaRepository.save(TestEntityFactory.createRoom(book2, TestEntityFactory.createScienceCategory()));
            // author는 otherRoom 참가자가 아님(또는 참가자라도 기록은 원래 room 소속)
        }

        @Test
        @DisplayName("roomId 불일치 → 403 Forbidden")
        void update_record_wrong_room_id() throws Exception {
            Map<String, Object> body = Map.of("content", "방 불일치 수정 시도");
            String prev = record.getContent();

            mockMvc.perform(patch("/rooms/{roomId}/records/{recordId}", otherRoom.getRoomId(), record.getPostId())
                            .requestAttr("userId", author.getUserId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").exists());

            RecordJpaEntity after = recordJpaRepository.findById(record.getPostId()).orElseThrow();
            assertThat(after.getContent()).isEqualTo(prev);
        }
    }
}