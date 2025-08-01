package konkuk.thip.vote.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import konkuk.thip.vote.adapter.in.web.request.VoteRequest;
import konkuk.thip.vote.adapter.out.jpa.VoteItemJpaEntity;
import konkuk.thip.vote.adapter.out.jpa.VoteJpaEntity;
import konkuk.thip.vote.adapter.out.persistence.repository.VoteItemJpaRepository;
import konkuk.thip.vote.adapter.out.persistence.repository.VoteJpaRepository;
import konkuk.thip.vote.adapter.out.persistence.repository.VoteParticipantJpaRepository;
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

import static konkuk.thip.common.exception.code.ErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@DisplayName("[통합] 투표하기 API 통합 테스트")
class VoteApiTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private AliasJpaRepository aliasJpaRepository;
    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private CategoryJpaRepository categoryJpaRepository;
    @Autowired private RoomJpaRepository roomJpaRepository;
    @Autowired private RoomParticipantJpaRepository roomParticipantJpaRepository;
    @Autowired private VoteJpaRepository voteJpaRepository;
    @Autowired private VoteItemJpaRepository voteItemJpaRepository;
    @Autowired private VoteParticipantJpaRepository voteParticipantJpaRepository;

    private AliasJpaEntity alias;
    private UserJpaEntity user;
    private CategoryJpaEntity category;
    private RoomJpaEntity room;
    private VoteJpaEntity vote;
    @Autowired
    private BookJpaRepository bookJpaRepository;

    @BeforeEach
    void setUp() {
        alias = aliasJpaRepository.save(TestEntityFactory.createScienceAlias());
        user = userJpaRepository.save(TestEntityFactory.createUser(alias));
        category = categoryJpaRepository.save(TestEntityFactory.createScienceCategory(alias));
        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook());
        room = roomJpaRepository.save(TestEntityFactory.createRoom(book, category));
        vote = voteJpaRepository.save(TestEntityFactory.createVote(user, room));
        roomParticipantJpaRepository.save(TestEntityFactory.createRoomParticipant(room, user, RoomParticipantRole.MEMBER, 0));
    }

    @Test
    @DisplayName("처음 투표 성공")
    void vote_first_success() throws Exception {
        VoteItemJpaEntity item = voteItemJpaRepository.save(
                VoteItemJpaEntity.builder()
                        .itemName("항목1")
                        .count(0)
                        .voteJpaEntity(vote)
                        .build()
        );

        VoteRequest request = new VoteRequest(item.getVoteItemId(), true);

        mockMvc.perform(post("/rooms/{roomId}/vote/{voteId}", room.getRoomId(), vote.getPostId())
                        .requestAttr("userId", user.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        assertThat(voteParticipantJpaRepository.findAll())
                .hasSize(1)
                .allMatch(vp -> vp.getVoteItemJpaEntity().getVoteItemId().equals(item.getVoteItemId()));

        voteItemJpaRepository.findById(item.getVoteItemId())
                .ifPresent(voteItem -> assertThat(voteItem.getCount()).isEqualTo(1));
    }

    @Test
    @DisplayName("이미 투표한 경우 다른 항목으로 변경 성공")
    void vote_alreadyVoted_change_success() throws Exception {
        VoteItemJpaEntity item1 = voteItemJpaRepository.save(
                VoteItemJpaEntity.builder().itemName("항목1").count(0).voteJpaEntity(vote).build()
        );
        VoteItemJpaEntity item2 = voteItemJpaRepository.save(
                VoteItemJpaEntity.builder().itemName("항목2").count(0).voteJpaEntity(vote).build()
        );

        voteParticipantJpaRepository.save(
                TestEntityFactory.createVoteParticipant(user, item1)
        );

        VoteRequest request = new VoteRequest(item2.getVoteItemId(), true);

        mockMvc.perform(post("/rooms/{roomId}/vote/{voteId}", room.getRoomId(), vote.getPostId())
                        .requestAttr("userId", user.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        assertThat(voteParticipantJpaRepository.findAll())
                .hasSize(1)
                .allMatch(vp -> vp.getVoteItemJpaEntity().getVoteItemId().equals(item2.getVoteItemId()));

        voteItemJpaRepository.findById(item1.getVoteItemId())
                .ifPresent(voteItem -> assertThat(voteItem.getCount()).isEqualTo(0));
    }

    @Test
    @DisplayName("같은 항목으로 다시 투표 시 예외")
    void vote_alreadyVoted_same_fail() throws Exception {
        VoteItemJpaEntity item1 = voteItemJpaRepository.save(
                VoteItemJpaEntity.builder().itemName("항목1").count(0).voteJpaEntity(vote).build()
        );

        voteParticipantJpaRepository.save(TestEntityFactory.createVoteParticipant(user, item1));

        VoteRequest request = new VoteRequest(item1.getVoteItemId(), true);

        mockMvc.perform(post("/rooms/{roomId}/vote/{voteId}", room.getRoomId(), vote.getPostId())
                        .requestAttr("userId", user.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(VOTE_ITEM_ALREADY_VOTED.getCode()));
    }

    @Test
    @DisplayName("투표 취소 성공")
    void vote_cancel_success() throws Exception {
        VoteItemJpaEntity item = voteItemJpaRepository.save(
                VoteItemJpaEntity.builder().itemName("항목1").count(1).voteJpaEntity(vote).build()
        );

        voteParticipantJpaRepository.save(TestEntityFactory.createVoteParticipant(user, item));

        VoteRequest request = new VoteRequest(item.getVoteItemId(), false);

        mockMvc.perform(post("/rooms/{roomId}/vote/{voteId}", room.getRoomId(), vote.getPostId())
                        .requestAttr("userId", user.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        assertThat(voteParticipantJpaRepository.findAll()).isEmpty();
        voteItemJpaRepository.findById(item.getVoteItemId())
                .ifPresent(voteItem -> assertThat(voteItem.getCount()).isEqualTo(0));
    }

    @Test
    @DisplayName("투표 취소 실패 - 기존 투표 기록 없음")
    void vote_cancel_withoutVote_fail() throws Exception {
        VoteItemJpaEntity item = voteItemJpaRepository.save(
                VoteItemJpaEntity.builder().itemName("항목1").count(0).voteJpaEntity(vote).build()
        );

        VoteRequest request = new VoteRequest(item.getVoteItemId(), false);

        mockMvc.perform(post("/rooms/{roomId}/vote/{voteId}", room.getRoomId(), vote.getPostId())
                        .requestAttr("userId", user.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(VOTE_ITEM_NOT_VOTED_CANNOT_CANCEL.getCode()));
    }

    @Test
    @DisplayName("방에 속하지 않은 사용자가 투표 시 예외")
    void vote_userNotRoomMember_fail() throws Exception {
        UserJpaEntity outsider = userJpaRepository.save(TestEntityFactory.createUser(alias));
        VoteItemJpaEntity item = voteItemJpaRepository.save(
                VoteItemJpaEntity.builder().itemName("항목1").count(0).voteJpaEntity(vote).build()
        );

        VoteRequest request = new VoteRequest(item.getVoteItemId(), true);

        mockMvc.perform(post("/rooms/{roomId}/vote/{voteId}", room.getRoomId(), vote.getPostId())
                        .requestAttr("userId", outsider.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ROOM_ACCESS_FORBIDDEN.getCode()));
    }
}