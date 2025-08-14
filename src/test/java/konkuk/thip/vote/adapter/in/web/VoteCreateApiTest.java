package konkuk.thip.vote.adapter.in.web;

import com.fasterxml.jackson.databind.JsonNode;
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
import konkuk.thip.vote.adapter.in.web.request.VoteCreateRequest;
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
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[통합] 투표 생성 api 통합 테스트")
class VoteCreateApiTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void tearDown() {
        voteItemJpaRepository.deleteAllInBatch();
        voteJpaRepository.deleteAllInBatch();
        roomParticipantJpaRepository.deleteAllInBatch();
        roomJpaRepository.deleteAllInBatch();
        bookJpaRepository.deleteAllInBatch();
        userJpaRepository.deleteAllInBatch();
        categoryJpaRepository.deleteAllInBatch();
        aliasJpaRepository.deleteAllInBatch();
    }

    private void saveUserAndRoom() {
        AliasJpaEntity alias = aliasJpaRepository.save(TestEntityFactory.createScienceAlias());
        UserJpaEntity user = userJpaRepository.save(TestEntityFactory.createUser(alias, "user"));

        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook());
        CategoryJpaEntity category = categoryJpaRepository.save(TestEntityFactory.createScienceCategory(alias));
        RoomJpaEntity room = roomJpaRepository.save(TestEntityFactory.createRoom(book, category));

        roomParticipantJpaRepository.save(TestEntityFactory.createRoomParticipant(room, user, RoomParticipantRole.MEMBER, 0.0));
    }

    @Test
    @DisplayName("[페이지 넘버, 총평 여부, 투표 내용, List<투표 항목>] 을 받아, 투표를 생성한다.")
    void vote_create_success() throws Exception {
        //given : user, room, request 생성
        saveUserAndRoom();

        int page = 10;
        boolean isOverview = false;
        String content = "투표 내용 입니다.";
        List<VoteCreateRequest.VoteItemCreateRequest> voteItems = List.of(
                new VoteCreateRequest.VoteItemCreateRequest("찬성"),
                new VoteCreateRequest.VoteItemCreateRequest("반대")
        );

        VoteCreateRequest request = new VoteCreateRequest(
                page, isOverview, content, voteItems
        );

        Long userId = userJpaRepository.findAll().get(0).getUserId();
        Long roomId = roomJpaRepository.findAll().get(0).getRoomId();

        //when : 투표 생성 api 호출 (filter 통과 없이)
        ResultActions result = mockMvc.perform(post("/rooms/{roomId}/vote", roomId)
                .requestAttr("userId", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)
                ));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.voteId").exists());

        String json = result.andReturn().getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(json);
        Long voteId = jsonNode.path("data").path("voteId").asLong();

        VoteJpaEntity voteJpaEntity = voteJpaRepository.findById(voteId).orElse(null);
        List<VoteItemJpaEntity> voteItemJpaEntityList = voteItemJpaRepository.findAllByVoteJpaEntity_PostId(voteJpaEntity.getPostId());

        assertThat(voteJpaEntity.getUserJpaEntity().getUserId()).isEqualTo(userId);
        assertThat(voteJpaEntity.getRoomJpaEntity().getRoomId()).isEqualTo(roomId);
        assertThat(voteJpaEntity.getPage()).isEqualTo(page);
        assertThat(voteJpaEntity.getContent()).isEqualTo(content);

        assertThat(voteItemJpaEntityList).hasSize(2)
                .extracting(VoteItemJpaEntity::getItemName)
                .containsExactlyInAnyOrder("찬성", "반대");
    }
}
