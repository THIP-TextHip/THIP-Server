package konkuk.thip.room.adapter.in.web;

import jakarta.persistence.EntityManager;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.exception.code.ErrorCode;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomParticipantJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomParticipantRole;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.roomparticipant.RoomParticipantJpaRepository;
import konkuk.thip.room.domain.Category;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserRole;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.domain.Alias;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[통합] 방 나가기(멤버) API 통합 테스트")
class RoomParticipantDeleteApiTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private EntityManager em;
    @Autowired private RoomJpaRepository roomJpaRepository;
    @Autowired private RoomParticipantJpaRepository roomParticipantJpaRepository;
    @Autowired private BookJpaRepository bookJpaRepository;
    @Autowired private UserJpaRepository userJpaRepository;

    private RoomJpaEntity room;
    private UserJpaEntity host;
    private UserJpaEntity participant;
    private RoomParticipantJpaEntity hostParticipation;
    private RoomParticipantJpaEntity memberParticipation;

    private void setUpWithOnlyHost() {
        Alias alias = TestEntityFactory.createLiteratureAlias();
        createUsers(alias);

        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook());
        Category category = TestEntityFactory.createLiteratureCategory();
        createRoom(book, category,1); // 방장만 포함
        roomParticipantJpaRepository.save(TestEntityFactory.createRoomParticipant(room, host, RoomParticipantRole.HOST, 0.0));
    }

    private void setUpWithParticipant() {
        Alias alias = TestEntityFactory.createLiteratureAlias();
        createUsers(alias);

        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook());
        Category category = TestEntityFactory.createLiteratureCategory();
        createRoom(book, category,2); // 방장과 참여자 포함
        hostParticipation = roomParticipantJpaRepository.save(TestEntityFactory.createRoomParticipant(room, host, RoomParticipantRole.HOST, 50.0));
        memberParticipation = roomParticipantJpaRepository.save(TestEntityFactory.createRoomParticipant(room, participant, RoomParticipantRole.MEMBER, 30.0));
    }

    private void createRoom(BookJpaEntity book, Category category, int memberCount) {
        room = roomJpaRepository.save(RoomJpaEntity.builder()
                .title("방이름")
                .description("설명")
                .isPublic(true)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(30))
                .recruitCount(3)
                .bookJpaEntity(book)
                .category(category)
                .memberCount(memberCount) // 방장과 참여자 포함
                .build());
    }

    private void createUsers(Alias alias) {
        host = userJpaRepository.save(UserJpaEntity.builder()
                .oauth2Id("kakao_432708231")
                .nickname("user")
                .nicknameUpdatedAt(LocalDate.now().minusMonths(7))
                .role(UserRole.USER)
                .alias(alias)
                .build());

        participant = userJpaRepository.save(UserJpaEntity.builder()
                .oauth2Id("kakao_12345678")
                .nickname("user123")
                .nicknameUpdatedAt(LocalDate.now().minusMonths(7))
                .role(UserRole.USER)
                .alias(alias)
                .build());
    }

    private void updateRoomPercentage() {
        hostParticipation.updateCurrentPage(50);
        hostParticipation.updateUserPercentage(50.0); // 방장의 진행도 50
        roomParticipantJpaRepository.save(hostParticipation);

        memberParticipation.updateCurrentPage(30);
        memberParticipation.updateUserPercentage(30.0); // 참여자 진행도 30
        roomParticipantJpaRepository.save(memberParticipation);

        room.updateRoomPercentage(40); // 방참여자들의 진행도 평균 40
        roomJpaRepository.save(room);
    }

    @Test
    @DisplayName("참여자가 정상적으로 방에서 나가면 유저의 방 참여 관계 삭제, 방 인원수 감소, 방 평균 진행도 업데이트 된다.")
    void leaveRoom_success() throws Exception {
        setUpWithParticipant();
        updateRoomPercentage();

        // 참여자(member)가 방에서 나가기 요청
        mockMvc.perform(delete("/rooms/" + room.getRoomId() + "/leave")
                        .requestAttr("userId", participant.getUserId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // 방에서 참여자 정보가 사라졌는지 확인
        boolean exists = roomParticipantJpaRepository.existsByUserIdAndRoomId(participant.getUserId(), room.getRoomId());
        assertThat(exists).isFalse();

        // 인원수 감소 확인
        RoomJpaEntity updateRoom = roomJpaRepository.findById(room.getRoomId()).orElseThrow();
        assertThat(updateRoom.getMemberCount()).isEqualTo(1); // 방장만 남음
        // 평균 진행도 확인: 방에 남은 사람은 host만 → host의 진행도(50)와 방의 진행도가 같아야 함
        assertThat(updateRoom.getRoomPercentage()).isEqualTo(50.0);
    }

    @Test
    @DisplayName("참여하지 않은 사용자가 방에서 나가기를 시도하면 실패한다.")
    void leaveRoom_notParticipated() throws Exception {
        setUpWithOnlyHost(); // 참가자 없이 방장만 있는 방

        // 참여하지 않은 사용자가 나가기 요청
        mockMvc.perform(delete("/rooms/" + room.getRoomId() + "/leave")
                        .requestAttr("userId", participant.getUserId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.ROOM_PARTICIPANT_NOT_FOUND.getCode()));
    }

    @Test
    @DisplayName("호스트(방장)는 방을 나갈 수 없고 예외가 발생한다.")
    void leaveRoom_hostCannotLeave() throws Exception {
        setUpWithOnlyHost();

        mockMvc.perform(delete("/rooms/" + room.getRoomId() + "/leave")
                        .requestAttr("userId", host.getUserId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.ROOM_HOST_CANNOT_LEAVE.getCode()));
    }
}
