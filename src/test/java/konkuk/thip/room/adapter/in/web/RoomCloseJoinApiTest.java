package konkuk.thip.room.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.domain.value.RoomParticipantRole;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.roomparticipant.RoomParticipantJpaRepository;
import konkuk.thip.room.domain.value.Category;
import konkuk.thip.room.domain.value.RoomStatus;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.domain.value.UserRole;
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
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
@DisplayName("[통합] 방 모집 마감 API 통합 테스트")
class RoomCloseJoinApiTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired RoomJpaRepository roomJpaRepository;
    @Autowired RoomParticipantJpaRepository roomParticipantJpaRepository;
    @Autowired BookJpaRepository bookJpaRepository;
    @Autowired UserJpaRepository userJpaRepository;

    private RoomJpaEntity room;
    private UserJpaEntity host;
    private UserJpaEntity member;
    private UserJpaEntity outsider;

    @BeforeEach
    void setup() {
        Alias alias = TestEntityFactory.createLiteratureAlias();

        host = userJpaRepository.save(UserJpaEntity.builder()
                .nickname("호스트")
                .nicknameUpdatedAt(LocalDate.now().minusMonths(7))
                .oauth2Id("kakao_12345678")
                .alias(alias)
                .role(UserRole.USER)
                .build());
        member = userJpaRepository.save(UserJpaEntity.builder()
                .nickname("방 참여자")
                .nicknameUpdatedAt(LocalDate.now().minusMonths(7))
                .oauth2Id("kakao_12345678")
                .alias(alias)
                .role(UserRole.USER)
                .build());
        outsider = userJpaRepository.save(UserJpaEntity.builder()
                .nickname("방 미참여자")
                .nicknameUpdatedAt(LocalDate.now().minusMonths(7))
                .oauth2Id("kakao_12345678")
                .alias(alias)
                .role(UserRole.USER)
                .build());

        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook());
        Category category = TestEntityFactory.createLiteratureCategory();

        room = roomJpaRepository.save(RoomJpaEntity.builder()
                .title("방 제목")
                .description("방 설명")
                .isPublic(true)
                .startDate(LocalDate.now().plusDays(3))
                .endDate(LocalDate.now().plusDays(33))
                .recruitCount(5)
                .memberCount(2)
                .bookJpaEntity(book)
                .category(category)
                .build());

        roomParticipantJpaRepository.save(TestEntityFactory.createRoomParticipant(room, host, RoomParticipantRole.HOST, 0.0));
        roomParticipantJpaRepository.save(TestEntityFactory.createRoomParticipant(room, member, RoomParticipantRole.MEMBER, 0.0));
    }

    @AfterEach
    void tearDown() {
        roomParticipantJpaRepository.deleteAllInBatch();
        roomJpaRepository.deleteAllInBatch();
        bookJpaRepository.deleteAllInBatch();
        userJpaRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("모집 마감 성공 - 방 시작일이 오늘로 바뀜")
    void closeRoomRecruit_success() throws Exception {
        mockMvc.perform(post("/rooms/" + room.getRoomId() + "/close")
                        .requestAttr("userId", host.getUserId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        RoomJpaEntity updatedRoom = roomJpaRepository.findById(room.getRoomId()).orElseThrow();
        assertThat(updatedRoom.getStartDate()).isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("비참여자가 모집 마감 요청 시 실패")
    void closeRoomRecruit_fail_not_participated() throws Exception {
        mockMvc.perform(post("/rooms/" + room.getRoomId() + "/close")
                        .requestAttr("userId", outsider.getUserId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("일반 멤버가 모집 마감 요청 시 실패")
    void closeRoomRecruit_fail_not_host() throws Exception {
        mockMvc.perform(post("/rooms/" + room.getRoomId() + "/close")
                        .requestAttr("userId", member.getUserId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("이미 시작된 방은 모집 마감 요청 시 실패")
    void closeRoomRecruit_fail_already_started() throws Exception {
        // startDate를 오늘로 설정해서 이미 시작된 상태로 만듦
        Field field = RoomJpaEntity.class.getDeclaredField("roomStatus");
        field.setAccessible(true);
        field.set(room, RoomStatus.IN_PROGRESS);
        roomJpaRepository.save(room);

        mockMvc.perform(post("/rooms/" + room.getRoomId() + "/close")
                        .requestAttr("userId", host.getUserId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
