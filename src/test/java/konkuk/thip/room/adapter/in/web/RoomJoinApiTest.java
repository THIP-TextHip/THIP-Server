package konkuk.thip.room.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.room.adapter.out.jpa.CategoryJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomParticipantJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomParticipantRole;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.category.CategoryJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.roomparticipant.RoomParticipantJpaRepository;
import konkuk.thip.user.adapter.out.jpa.AliasJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserRole;
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

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static konkuk.thip.room.domain.RoomJoinType.CANCEL;
import static konkuk.thip.room.domain.RoomJoinType.JOIN;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("방 참여/취소 API 통합 테스트")
class RoomJoinApiTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private RoomJpaRepository roomJpaRepository;
    @Autowired private RoomParticipantJpaRepository roomParticipantJpaRepository;
    @Autowired private BookJpaRepository bookJpaRepository;
    @Autowired private CategoryJpaRepository categoryJpaRepository;
    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private AliasJpaRepository aliasJpaRepository;

    private RoomJpaEntity room;
    private UserJpaEntity host;
    private UserJpaEntity participant;

    private void setUpWithOnlyHost() {
        AliasJpaEntity alias = aliasJpaRepository.save(TestEntityFactory.createLiteratureAlias());
        createUsers(alias);

        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook());
        CategoryJpaEntity category = categoryJpaRepository.save(TestEntityFactory.createLiteratureCategory(alias));
        createRoom(book, category,1); // 방장만 포함
        roomParticipantJpaRepository.save(TestEntityFactory.createUserRoom(room, host, RoomParticipantRole.HOST, 0.0));
    }

    private void setUpWithParticipant() {
        AliasJpaEntity alias = aliasJpaRepository.save(TestEntityFactory.createLiteratureAlias());
        createUsers(alias);

        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook());
        CategoryJpaEntity category = categoryJpaRepository.save(TestEntityFactory.createLiteratureCategory(alias));
        createRoom(book, category,2); // 방장과 참여자 포함
        roomParticipantJpaRepository.save(TestEntityFactory.createUserRoom(room, host, RoomParticipantRole.HOST, 0.0));
        roomParticipantJpaRepository.save(TestEntityFactory.createUserRoom(room, participant, RoomParticipantRole.MEMBER, 0.0));
    }

    private void createRoom(BookJpaEntity book, CategoryJpaEntity category, int memberCount) {
        room = roomJpaRepository.save(RoomJpaEntity.builder()
                .title("방이름")
                .description("설명")
                .isPublic(true)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(5))
                .recruitCount(3)
                .bookJpaEntity(book)
                .categoryJpaEntity(category)
                .memberCount(memberCount) // 방장과 참여자 포함
                .build());
    }

    private void createUsers(AliasJpaEntity alias) {
        host = userJpaRepository.save(UserJpaEntity.builder()
                .oauth2Id("kakao_432708231")
                .nickname("user")
                .imageUrl("img")
                .role(UserRole.USER)
                .aliasForUserJpaEntity(alias)
                .build());

        participant = userJpaRepository.save(UserJpaEntity.builder()
                .oauth2Id("kakao_12345678")
                .nickname("user123")
                .imageUrl("img")
                .role(UserRole.USER)
                .aliasForUserJpaEntity(alias)
                .build());
    }

    @AfterEach
    void tearDown() {
        roomParticipantJpaRepository.deleteAllInBatch();
        roomJpaRepository.deleteAll();
        categoryJpaRepository.deleteAll();
        bookJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
        aliasJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("방 참여 성공 - 참여자 저장 및 인원수 증가 확인")
    void joinRoom_success() throws Exception {
        setUpWithOnlyHost();
        Map<String, Object> request = new HashMap<>();
        request.put("type", JOIN.getType());

        mockMvc.perform(post("/rooms/" + room.getRoomId() + "/join")
                        .requestAttr("userId", participant.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // 참여자 저장 확인
        RoomParticipantJpaEntity roomParticipantJpaEntity = roomParticipantJpaRepository
                .findByUserIdAndRoomId(participant.getUserId(), room.getRoomId())
                .orElse(null);
        assertThat(roomParticipantJpaEntity).isNotNull();

        // 인원수 증가 확인
        room = roomJpaRepository.findById(room.getRoomId()).orElseThrow();
        assertThat(room.getMemberCount()).isEqualTo(2); // 방 생성 시 1명 + 참여 1명
    }


    @Test
    @DisplayName("방 중복 참여 실패")
    void joinRoom_alreadyParticipated() throws Exception {
        // 이미 참여한 상태로 설정
        setUpWithParticipant();

        Map<String, Object> request = new HashMap<>();
        request.put("type", JOIN.getType());

        ResultActions result = mockMvc.perform(post("/rooms/" + room.getRoomId() + "/join")
                .requestAttr("userId", participant.getUserId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("방 참여 취소 성공 - 참여자 제거 및 인원수 감소 확인")
    void cancelJoin_success() throws Exception {
        // 이미 참여한 상태로 설정
        setUpWithParticipant();

        Map<String, Object> request = new HashMap<>();
        request.put("type", CANCEL.getType());

        mockMvc.perform(post("/rooms/" + room.getRoomId() + "/join")
                        .requestAttr("userId", participant.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // 참여자 삭제 확인
        boolean exists = roomParticipantJpaRepository
                .existByUserIdAndRoomId(participant.getUserId(), room.getRoomId());
        assertThat(exists).isFalse();

        // 인원수 감소 확인
        room = roomJpaRepository.findById(room.getRoomId()).orElseThrow();
        assertThat(room.getMemberCount()).isEqualTo(1); // 다시 원래 인원
    }

    @Test
    @DisplayName("방 미참여자 취소 실패")
    void cancelJoin_notParticipated() throws Exception {
        setUpWithOnlyHost();

        Map<String, Object> request = new HashMap<>();
        request.put("type", CANCEL.getType());

        ResultActions result = mockMvc.perform(post("/rooms/" + room.getRoomId() + "/join")
                .requestAttr("userId", participant.getUserId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isBadRequest());
    }
}