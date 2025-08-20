package konkuk.thip.room.adapter.in.web;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.util.DateUtil;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomParticipantRole;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.roomparticipant.RoomParticipantJpaRepository;
import konkuk.thip.room.domain.value.Category;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.domain.value.Alias;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * /rooms?category=문학 API 통합 테스트
 * - 마감 임박, 인기 방 조회
 * - 내가 참여한 방은 제외
 * - 비공개 방은 제외
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[통합] 마감 임박 및 인기 방 조회 API 통합 테스트")
@Transactional
class RoomGetDeadlinePopularApiTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private RoomJpaRepository roomJpaRepository;
    @Autowired private RoomParticipantJpaRepository participantJpaRepository;
    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private BookJpaRepository bookJpaRepository;

    private Alias alias;
    private Category category;
    private UserJpaEntity currentUser;
    private BookJpaEntity book;
    private final List<RoomJpaEntity> rooms = new ArrayList<>();

    private final int maxMemberCount = 30; // 인기 방의 최대 인원 수
    private final LocalDate today = LocalDate.now();

    private RoomJpaEntity privateRoom;
    private RoomJpaEntity joinedRoom;

    @BeforeEach
    void setUp() {
        alias = TestEntityFactory.createLiteratureAlias();
        category = TestEntityFactory.createLiteratureCategory();
        currentUser = userJpaRepository.save(TestEntityFactory.createUser(alias, "현재유저"));
        book = bookJpaRepository.save(TestEntityFactory.createBook());

        // startDate > 오늘 조건을 만족하도록 +1일부터 생성
        for (int i = 0; i < 5; i++) {
            RoomJpaEntity deadlineRoom = TestEntityFactory.createRoom(book, category);
            deadlineRoom.updateStartDate(today.plusDays(i + 1));
            deadlineRoom.updateMemberCount(5);
            rooms.add(roomJpaRepository.save(deadlineRoom));
        }

        for (int i = 0; i < 5; i++) {
            RoomJpaEntity popularRoom = TestEntityFactory.createRoom(book, category);
            popularRoom.updateStartDate(today.plusDays(10 + i));
            popularRoom.updateMemberCount(maxMemberCount - i);
            rooms.add(roomJpaRepository.save(popularRoom));
        }

        // 오늘 날짜 방 (조건 불만족)
        RoomJpaEntity expiredRoom = TestEntityFactory.createRoom(book, category);
        expiredRoom.updateStartDate(today);
        expiredRoom.updateIsPublic(true);
        roomJpaRepository.save(expiredRoom);

        // 비공개 방 (조건 불만족)
        privateRoom = TestEntityFactory.createRoom(book, category);
        privateRoom.updateStartDate(today.plusDays(2));
        privateRoom.updateIsPublic(false);
        roomJpaRepository.save(privateRoom);

        // 내가 참여한 방 (조건 불만족)
        joinedRoom = TestEntityFactory.createRoom(book, category);
        joinedRoom.updateStartDate(today.plusDays(5));
        joinedRoom = roomJpaRepository.save(joinedRoom);
        participantJpaRepository.save(
                TestEntityFactory.createRoomParticipant(joinedRoom, currentUser, RoomParticipantRole.MEMBER, 0)
        );
    }

    @Test
    @DisplayName("카테고리별로 마감 임박 방 4개와 인기 방 4개를 조회하고 조건과 정렬을 검증한다")
    void getDeadlineAndPopularRooms() throws Exception {
        mockMvc.perform(
                        get("/rooms")
                                .param("category", "문학")
                                .requestAttr("userId", currentUser.getUserId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.deadlineRoomList", hasSize(4)))
                .andExpect(jsonPath("$.data.deadlineRoomList[0].deadlineDate").value(DateUtil.formatAfterTime(today.plusDays(1))))
                .andExpect(jsonPath("$.data.popularRoomList", hasSize(4)))
                .andExpect(jsonPath("$.data.popularRoomList[0].memberCount").value(maxMemberCount))
                .andExpect(jsonPath("$.data.deadlineRoomList[0].roomId").isNumber())
                .andExpect(jsonPath("$.data.deadlineRoomList[0].bookImageUrl").isString())
                .andExpect(jsonPath("$.data.deadlineRoomList[0].roomName").isString())
                .andExpect(jsonPath("$.data.deadlineRoomList[0].memberCount").isNumber())
                .andExpect(jsonPath("$.data.deadlineRoomList[0].deadlineDate").isString());
    }

    @Test
    @DisplayName("내가 참여한 방은 조회 결과에 포함되지 않는다")
    void joinedRoomIsExcluded() throws Exception {
        mockMvc.perform(
                        get("/rooms")
                                .param("category", "문학")
                                .requestAttr("userId", currentUser.getUserId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.deadlineRoomList[*].roomId", not(hasItem(joinedRoom.getRoomId().intValue()))))
                .andExpect(jsonPath("$.data.popularRoomList[*].roomId", not(hasItem(joinedRoom.getRoomId().intValue()))));
    }

    @Test
    @DisplayName("비공개 방은 조회 결과에 포함되지 않는다")
    void privateRoomIsExcluded() throws Exception {
        mockMvc.perform(
                        get("/rooms")
                                .param("category", "문학")
                                .requestAttr("userId", currentUser.getUserId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.deadlineRoomList[*].roomId", not(hasItem(privateRoom.getRoomId().intValue()))))
                .andExpect(jsonPath("$.data.popularRoomList[*].roomId", not(hasItem(privateRoom.getRoomId().intValue()))));
    }
}