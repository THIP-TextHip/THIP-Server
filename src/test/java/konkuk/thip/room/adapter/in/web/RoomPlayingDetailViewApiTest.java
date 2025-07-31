package konkuk.thip.room.adapter.in.web;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.util.DateUtil;
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
import konkuk.thip.vote.adapter.out.jpa.VoteItemJpaEntity;
import konkuk.thip.vote.adapter.out.jpa.VoteJpaEntity;
import konkuk.thip.vote.adapter.out.persistence.repository.VoteItemJpaRepository;
import konkuk.thip.vote.adapter.out.persistence.repository.VoteJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import static konkuk.thip.common.exception.code.ErrorCode.ROOM_ACCESS_FORBIDDEN;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[통합] 진행 중인 방 상세조회 api 통합 테스트")
class RoomPlayingDetailViewApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AliasJpaRepository aliasJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

    @Autowired
    private BookJpaRepository bookJpaRepository;

    @Autowired
    private RoomJpaRepository roomJpaRepository;

    @Autowired
    private RoomParticipantJpaRepository roomParticipantJpaRepository;

    @Autowired
    private VoteJpaRepository voteJpaRepository;

    @Autowired
    private VoteItemJpaRepository voteItemJpaRepository;

    @AfterEach
    void tearDown() {
        voteItemJpaRepository.deleteAll();
        voteJpaRepository.deleteAll();
        roomParticipantJpaRepository.deleteAllInBatch();
        roomJpaRepository.deleteAll();
        bookJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
        categoryJpaRepository.deleteAll();
        aliasJpaRepository.deleteAll();
    }

    private RoomJpaEntity saveScienceRoom(String bookTitle, String isbn, String roomName, LocalDate startDate, int recruitCount) {
        AliasJpaEntity alias = aliasJpaRepository.save(TestEntityFactory.createScienceAlias());

        BookJpaEntity book = bookJpaRepository.save(BookJpaEntity.builder()
                .title(bookTitle)
                .isbn(isbn)
                .authorName("한강")
                .bestSeller(false)
                .publisher("문학동네")
                .imageUrl("https://image1.jpg")
                .pageCount(300)
                .description("한강의 소설")
                .build());

        CategoryJpaEntity category = categoryJpaRepository.save(TestEntityFactory.createScienceCategory(alias));

        return roomJpaRepository.save(RoomJpaEntity.builder()
                .title(roomName)
                .description("한강 작품 읽기 모임")
                .isPublic(true)
                .roomPercentage(0.0)
                .startDate(startDate)
                .endDate(LocalDate.now().plusDays(30))
                .recruitCount(recruitCount)
                .bookJpaEntity(book)
                .categoryJpaEntity(category)
                .build());
    }

    private void saveUsersToRoom(RoomJpaEntity roomJpaEntity, int count) {
        AliasJpaEntity alias = aliasJpaRepository.save(TestEntityFactory.createScienceAlias());

        // User 리스트 생성 및 저장
        List<UserJpaEntity> users = IntStream.rangeClosed(1, count)
                .mapToObj(i -> UserJpaEntity.builder()
                        .nickname("user" + i)
                        .imageUrl("http://image")
                        .oauth2Id("oauth2Id")
                        .role(UserRole.USER)
                        .aliasForUserJpaEntity(alias)
                        .build())
                .toList();

        List<UserJpaEntity> savedUsers = userJpaRepository.saveAll(users);

        // UserRoom 매핑 리스트 생성 및 저장
        List<RoomParticipantJpaEntity> mappings = savedUsers.stream()
                .map(user -> RoomParticipantJpaEntity.builder()
                        .userJpaEntity(user)
                        .roomJpaEntity(roomJpaEntity)
                        .roomParticipantRole(RoomParticipantRole.MEMBER)
                        .build())
                .toList();

        roomParticipantJpaRepository.saveAll(mappings);
    }

    private void createVoteToRoom(UserJpaEntity creator, RoomJpaEntity roomJpaEntity, int count) {
        for (int v = 1; v <= count; v++) {
            VoteJpaEntity voteJpaEntity = voteJpaRepository.save(
                    VoteJpaEntity.builder()
                            .content("vote-content-" + v)
                            .likeCount(0)
                            .commentCount(0)
                            .userJpaEntity(creator)
                            .page(v * 10)
                            .isOverview(false)
                            .roomJpaEntity(roomJpaEntity)
                            .build()
            );

            for (int vi = 1; vi <= 2; vi++) {
                voteItemJpaRepository.save(
                        VoteItemJpaEntity.builder()
                                .itemName("item-" + v + "-" + vi)
                                .count(v * 10)      // v값이 클수록 해당 투표의 투표항목을 선택한 사람 수가 많다 == 해당 투표의 참여율이 높다
                                .voteJpaEntity(voteJpaEntity)
                                .build()
                );
            }
        }
    }

    @Test
    @DisplayName("진행중인 모임방 상세조회할 경우, [해당 모임방의 정보, 책 정보, 유저의 현재 활동 정보, 현재 진행중인 투표]를 반환한다.")
    void get_playing_room_detail() throws Exception {
        //given
        RoomJpaEntity room = saveScienceRoom("과학-책", "isbn1", "과학-방-1일뒤-활동시작", LocalDate.now().plusDays(1), 10);
        saveUsersToRoom(room, 4);
        RoomParticipantJpaEntity roomParticipantJpaEntity = roomParticipantJpaRepository.findAllByRoomId(room.getRoomId()).get(0);
        roomParticipantJpaRepository.delete(roomParticipantJpaEntity);
        RoomParticipantJpaEntity joiningMember = roomParticipantJpaRepository.save(RoomParticipantJpaEntity.builder()
                .userJpaEntity(roomParticipantJpaEntity.getUserJpaEntity())
                .roomJpaEntity(roomParticipantJpaEntity.getRoomJpaEntity())
                .roomParticipantRole(RoomParticipantRole.MEMBER)        // Member
                .currentPage(50)        // 현재 member의 마지막 활동 page
                .userPercentage(10.6)       // 현재 member의 활동 percentage
                .build());

        createVoteToRoom(joiningMember.getUserJpaEntity(), room, 2);      // 2개의 투표 생성

        //when
        ResultActions result = mockMvc.perform(get("/rooms/{roomId}/playing", room.getRoomId())
                .requestAttr("userId", joiningMember.getUserJpaEntity().getUserId()));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isHost", is(false)))
                .andExpect(jsonPath("$.data.roomName", is("과학-방-1일뒤-활동시작")))
                .andExpect(jsonPath("$.data.roomImageUrl", is("과학/IT_image")))      // 방 대표 이미지 추가
                .andExpect(jsonPath("$.data.progressStartDate", is(DateUtil.formatDate(LocalDate.now().plusDays(1)))))
                .andExpect(jsonPath("$.data.memberCount", is(4)))
                .andExpect(jsonPath("$.data.recruitCount", is(10)))
                .andExpect(jsonPath("$.data.isbn", is("isbn1")))
                .andExpect(jsonPath("$.data.bookTitle", is("과학-책")))
                .andExpect(jsonPath("$.data.currentPage", is(50)))
                .andExpect(jsonPath("$.data.userPercentage", is(10.6)))
                .andExpect(jsonPath("$.data.currentVotes", hasSize(2)))
                /**
                 * currentVotes 검증 : 현재 모임방의 참여율이 높은 투표와 투표 항목들을 노출
                 * <정렬 순서> : 투표 참여율 높은 순 (vote 2 -> vote 1 순)
                 */
                .andExpect(jsonPath("$.data.currentVotes[0].content", is("vote-content-2")))
                .andExpect(jsonPath("$.data.currentVotes[0].voteItems[0].itemName", is("item-2-1")))
                .andExpect(jsonPath("$.data.currentVotes[0].voteItems[1].itemName", is("item-2-2")))

                .andExpect(jsonPath("$.data.currentVotes[1].content", is("vote-content-1")))
                .andExpect(jsonPath("$.data.currentVotes[1].voteItems[0].itemName", is("item-1-1")))
                .andExpect(jsonPath("$.data.currentVotes[1].voteItems[1].itemName", is("item-1-2")));
    }

    @Test
    @DisplayName("모임방의 호스트가 조회할 경우, 유저가 해당 방의 호스트임을 응답값으로 보여준다. (나머지 응답값은 동일)")
    void get_playing_room_detail_host() throws Exception {
        //given
        RoomJpaEntity room = saveScienceRoom("과학-책", "isbn1", "과학-방-1일뒤-활동시작", LocalDate.now().plusDays(1), 10);
        saveUsersToRoom(room, 4);
        RoomParticipantJpaEntity roomParticipantJpaEntity = roomParticipantJpaRepository.findAllByRoomId(room.getRoomId()).get(0);
        roomParticipantJpaRepository.delete(roomParticipantJpaEntity);
        RoomParticipantJpaEntity roomHost = roomParticipantJpaRepository.save(RoomParticipantJpaEntity.builder()
                .userJpaEntity(roomParticipantJpaEntity.getUserJpaEntity())
                .roomJpaEntity(roomParticipantJpaEntity.getRoomJpaEntity())
                .roomParticipantRole(RoomParticipantRole.HOST)        // HOST
                .currentPage(50)        // 현재 member의 마지막 활동 page
                .userPercentage(10.6)       // 현재 member의 활동 percentage
                .build());

        createVoteToRoom(roomHost.getUserJpaEntity(), room, 2);      // 2개의 투표 생성

        //when
        ResultActions result = mockMvc.perform(get("/rooms/{roomId}/playing", room.getRoomId())
                .requestAttr("userId", roomHost.getUserJpaEntity().getUserId()));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isHost", is(true)))     // 방 HOST 이면 true
                .andExpect(jsonPath("$.data.roomName", is("과학-방-1일뒤-활동시작")))
                .andExpect(jsonPath("$.data.roomImageUrl", is("과학/IT_image")))      // 방 대표 이미지 추가
                .andExpect(jsonPath("$.data.progressStartDate", is(DateUtil.formatDate(LocalDate.now().plusDays(1)))))
                .andExpect(jsonPath("$.data.memberCount", is(4)))
                .andExpect(jsonPath("$.data.recruitCount", is(10)))
                .andExpect(jsonPath("$.data.isbn", is("isbn1")))
                .andExpect(jsonPath("$.data.bookTitle", is("과학-책")))
                .andExpect(jsonPath("$.data.currentPage", is(50)))
                .andExpect(jsonPath("$.data.userPercentage", is(10.6)))
                .andExpect(jsonPath("$.data.currentVotes", hasSize(2)))
                /**
                 * currentVotes 검증 : 현재 모임방의 참여율이 높은 투표와 투표 항목들을 노출
                 * <정렬 순서> : 투표 참여율 높은 순 (vote 2 -> vote 1 순)
                 */
                .andExpect(jsonPath("$.data.currentVotes[0].content", is("vote-content-2")))
                .andExpect(jsonPath("$.data.currentVotes[0].voteItems[0].itemName", is("item-2-1")))
                .andExpect(jsonPath("$.data.currentVotes[0].voteItems[1].itemName", is("item-2-2")))

                .andExpect(jsonPath("$.data.currentVotes[1].content", is("vote-content-1")))
                .andExpect(jsonPath("$.data.currentVotes[1].voteItems[0].itemName", is("item-1-1")))
                .andExpect(jsonPath("$.data.currentVotes[1].voteItems[1].itemName", is("item-1-2")));
    }

    @Test
    @DisplayName("모임방에 속하지 않는 유저가 진행중인 모임방 상세조회를 요청한 경우, 400 error 발생한다.")
    void get_playing_room_detail_not_belong_to_room() throws Exception {
        //given
        RoomJpaEntity room = saveScienceRoom("과학-책", "isbn1", "과학-방-1일뒤-활동시작", LocalDate.now().plusDays(1), 10);
        saveUsersToRoom(room, 4);
        RoomParticipantJpaEntity roomParticipantJpaEntity = roomParticipantJpaRepository.findAllByRoomId(room.getRoomId()).get(0);
        roomParticipantJpaRepository.delete(roomParticipantJpaEntity);
        RoomParticipantJpaEntity joiningMember = roomParticipantJpaRepository.save(RoomParticipantJpaEntity.builder()
                .userJpaEntity(roomParticipantJpaEntity.getUserJpaEntity())
                .roomJpaEntity(roomParticipantJpaEntity.getRoomJpaEntity())
                .roomParticipantRole(RoomParticipantRole.MEMBER)        // Member
                .currentPage(50)        // 현재 member의 마지막 활동 page
                .userPercentage(10.6)       // 현재 member의 활동 percentage
                .build());

        createVoteToRoom(joiningMember.getUserJpaEntity(), room, 2);      // 2개의 투표 생성

        //when //then
        mockMvc.perform(get("/rooms/{roomId}/playing", room.getRoomId())
                        .requestAttr("userId", 1000L))      // 방에 속하지 않는 유저
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ROOM_ACCESS_FORBIDDEN.getCode()))
                .andExpect(jsonPath("$.message", containsString(ROOM_ACCESS_FORBIDDEN.getMessage())));
    }

    @Test
    @DisplayName("모임방에서 진행중인 투표가 많을 경우, 참여율이 높은 순으로 최대 3개의 투표만 보여준다.")
    void get_playing_room_detail_too_many_votes() throws Exception {
        //given
        RoomJpaEntity room = saveScienceRoom("과학-책", "isbn1", "과학-방-1일뒤-활동시작", LocalDate.now().plusDays(1), 10);
        saveUsersToRoom(room, 4);
        RoomParticipantJpaEntity roomParticipantJpaEntity = roomParticipantJpaRepository.findAllByRoomId(room.getRoomId()).get(0);
        roomParticipantJpaRepository.delete(roomParticipantJpaEntity);
        RoomParticipantJpaEntity joiningMember = roomParticipantJpaRepository.save(RoomParticipantJpaEntity.builder()
                .userJpaEntity(roomParticipantJpaEntity.getUserJpaEntity())
                .roomJpaEntity(roomParticipantJpaEntity.getRoomJpaEntity())
                .roomParticipantRole(RoomParticipantRole.MEMBER)        // Member
                .currentPage(50)        // 현재 member의 마지막 활동 page
                .userPercentage(10.6)       // 현재 member의 활동 percentage
                .build());

        createVoteToRoom(joiningMember.getUserJpaEntity(), room, 6);      // 6개의 투표 생성

        //when
        ResultActions result = mockMvc.perform(get("/rooms/{roomId}/playing", room.getRoomId())
                .requestAttr("userId", joiningMember.getUserJpaEntity().getUserId()));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isHost", is(false)))
                .andExpect(jsonPath("$.data.roomName", is("과학-방-1일뒤-활동시작")))
                .andExpect(jsonPath("$.data.roomImageUrl", is("과학/IT_image")))      // 방 대표 이미지 추가
                .andExpect(jsonPath("$.data.progressStartDate", is(DateUtil.formatDate(LocalDate.now().plusDays(1)))))
                .andExpect(jsonPath("$.data.memberCount", is(4)))
                .andExpect(jsonPath("$.data.recruitCount", is(10)))
                .andExpect(jsonPath("$.data.isbn", is("isbn1")))
                .andExpect(jsonPath("$.data.bookTitle", is("과학-책")))
                .andExpect(jsonPath("$.data.currentPage", is(50)))
                .andExpect(jsonPath("$.data.userPercentage", is(10.6)))
                .andExpect(jsonPath("$.data.currentVotes", hasSize(3)))
                /**
                 * currentVotes 검증 : 현재 모임방의 참여율이 높은 투표와 투표 항목들을 노출
                 * <정렬 순서> : 투표 참여율 높은 순 (vote 6 -> vote 5 -> vote 4 순)
                 */
                .andExpect(jsonPath("$.data.currentVotes[0].content", is("vote-content-6")))
                .andExpect(jsonPath("$.data.currentVotes[0].voteItems[0].itemName", is("item-6-1")))
                .andExpect(jsonPath("$.data.currentVotes[0].voteItems[1].itemName", is("item-6-2")))

                .andExpect(jsonPath("$.data.currentVotes[1].content", is("vote-content-5")))
                .andExpect(jsonPath("$.data.currentVotes[1].voteItems[0].itemName", is("item-5-1")))
                .andExpect(jsonPath("$.data.currentVotes[1].voteItems[1].itemName", is("item-5-2")))

                .andExpect(jsonPath("$.data.currentVotes[2].content", is("vote-content-4")))
                .andExpect(jsonPath("$.data.currentVotes[2].voteItems[0].itemName", is("item-4-1")))
                .andExpect(jsonPath("$.data.currentVotes[2].voteItems[1].itemName", is("item-4-2")));
    }

    @Test
    @DisplayName("모임방에서 진행중인 투표가 없을 경우, 빈 리스트를 보여준다.")
    void get_playing_room_detail_no_votes() throws Exception {
        //given
        RoomJpaEntity room = saveScienceRoom("과학-책", "isbn1", "과학-방-1일뒤-활동시작", LocalDate.now().plusDays(1), 10);
        saveUsersToRoom(room, 4);
        RoomParticipantJpaEntity roomParticipantJpaEntity = roomParticipantJpaRepository.findAllByRoomId(room.getRoomId()).get(0);
        roomParticipantJpaRepository.delete(roomParticipantJpaEntity);
        RoomParticipantJpaEntity joiningMember = roomParticipantJpaRepository.save(RoomParticipantJpaEntity.builder()
                .userJpaEntity(roomParticipantJpaEntity.getUserJpaEntity())
                .roomJpaEntity(roomParticipantJpaEntity.getRoomJpaEntity())
                .roomParticipantRole(RoomParticipantRole.MEMBER)        // Member
                .currentPage(50)        // 현재 member의 마지막 활동 page
                .userPercentage(10.6)       // 현재 member의 활동 percentage
                .build());

        createVoteToRoom(joiningMember.getUserJpaEntity(), room, 0);      // 투표 생성 X

        //when
        ResultActions result = mockMvc.perform(get("/rooms/{roomId}/playing", room.getRoomId())
                .requestAttr("userId", joiningMember.getUserJpaEntity().getUserId()));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isHost", is(false)))
                .andExpect(jsonPath("$.data.roomName", is("과학-방-1일뒤-활동시작")))
                .andExpect(jsonPath("$.data.roomImageUrl", is("과학/IT_image")))      // 방 대표 이미지 추가
                .andExpect(jsonPath("$.data.progressStartDate", is(DateUtil.formatDate(LocalDate.now().plusDays(1)))))
                .andExpect(jsonPath("$.data.memberCount", is(4)))
                .andExpect(jsonPath("$.data.recruitCount", is(10)))
                .andExpect(jsonPath("$.data.isbn", is("isbn1")))
                .andExpect(jsonPath("$.data.bookTitle", is("과학-책")))
                .andExpect(jsonPath("$.data.currentPage", is(50)))
                .andExpect(jsonPath("$.data.userPercentage", is(10.6)))
                .andExpect(jsonPath("$.data.currentVotes", hasSize(0)));        // 투표 없음
    }
}
