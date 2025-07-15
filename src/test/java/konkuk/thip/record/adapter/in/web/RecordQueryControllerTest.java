package konkuk.thip.record.adapter.in.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.util.DateUtil;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.record.adapter.out.jpa.RecordJpaEntity;
import konkuk.thip.record.adapter.out.persistence.repository.RecordJpaRepository;
import konkuk.thip.room.adapter.out.jpa.CategoryJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.adapter.out.persistence.repository.CategoryJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.user.adapter.out.jpa.AliasJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserRole;
import konkuk.thip.user.adapter.out.persistence.repository.AliasJpaRepository;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.vote.adapter.out.jpa.VoteItemJpaEntity;
import konkuk.thip.vote.adapter.out.jpa.VoteJpaEntity;
import konkuk.thip.vote.adapter.out.persistence.VoteItemJpaRepository;
import konkuk.thip.vote.adapter.out.persistence.VoteJpaRepository;
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
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("[통합] RecordSearchController 테스트")
@AutoConfigureMockMvc(addFilters = false)
class RecordSearchControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private VoteJpaRepository voteJpaRepository;
    @Autowired private VoteItemJpaRepository voteItemJpaRepository;
    @Autowired private RecordJpaRepository recordJpaRepository;
    @Autowired private RoomJpaRepository roomJpaRepository;
    @Autowired private BookJpaRepository bookJpaRepository;
    @Autowired private CategoryJpaRepository categoryJpaRepository;
    @Autowired private AliasJpaRepository aliasJpaRepository;
    @Autowired private UserJpaRepository userJpaRepository;

    @AfterEach
    void tearDown() {
        voteItemJpaRepository.deleteAll();
        voteJpaRepository.deleteAll();
        recordJpaRepository.deleteAll();
        roomJpaRepository.deleteAll();
        bookJpaRepository.deleteAll();
        categoryJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
        aliasJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("기록장 조회 시 record와 vote 모두 조회")
    void record_with_vote_response_success() throws Exception {
        // given
        AliasJpaEntity alias = aliasJpaRepository.save(TestEntityFactory.createLiteratureAlias());

        UserJpaEntity user = userJpaRepository.save(UserJpaEntity.builder()
                .oauth2Id("kakao_123")
                .nickname("사용자")
                .imageUrl("http://user.img")
                .role(UserRole.USER)
                .aliasForUserJpaEntity(alias)
                .build());

        BookJpaEntity book = bookJpaRepository.save(BookJpaEntity.builder()
                .isbn("1234567890")
                .title("테스트책")
                .authorName("작가")
                .publisher("출판사")
                .pageCount(200)
                .description("책 설명")
                .imageUrl("http://book.img")
                .bestSeller(false)
                .build());

        CategoryJpaEntity category = categoryJpaRepository.save(TestEntityFactory.createLiteratureCategory(alias));

        RoomJpaEntity room = roomJpaRepository.save(RoomJpaEntity.builder()
                .title("방 제목")
                .description("설명")
                .isPublic(true)
                .roomPercentage(0.0)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(30))
                .recruitCount(5)
                .bookJpaEntity(book)
                .categoryJpaEntity(category)
                .build());

        RecordJpaEntity record = recordJpaRepository.save(RecordJpaEntity.builder()
                .userJpaEntity(user)
                .roomJpaEntity(room)
                .likeCount(1)
                .commentCount(2)
                .page(1)
                .content("레코드 내용")
                .build());

        VoteJpaEntity vote = voteJpaRepository.save(VoteJpaEntity.builder()
                .userJpaEntity(user)
                .roomJpaEntity(room)
                .likeCount(1)
                .commentCount(2)
                .page(1)
                .content("투표 내용")
                .build());

        voteItemJpaRepository.save(VoteItemJpaEntity.builder()
                .voteJpaEntity(vote)
                .itemName("찬성")
                .count(3)
                .build());

        voteItemJpaRepository.save(VoteItemJpaEntity.builder()
                .voteJpaEntity(vote)
                .itemName("반대")
                .count(1)
                .build());

        // when
        ResultActions result = mockMvc.perform(get("/rooms/" + room.getRoomId() + "/posts")
                .requestAttr("userId", 1L)
                .param("type", "group")
                .param("sort", "latest")
                .param("pageStart", "1")
                .param("pageEnd", "10")
                .param("pageNum", "1")
                .param("isOverview", "false")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk());
        String json = result.andReturn().getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(json);

        JsonNode voteNode = jsonNode.path("data").path("recordList").get(0);
        assertThat(voteNode.path("type").asText()).isEqualTo("VOTE");
        assertThat(voteNode.path("page").asInt()).isEqualTo(1);
        assertThat(voteNode.path("content").asText()).isEqualTo("투표 내용");
        assertThat(voteNode.path("nickName").asText()).isEqualTo("사용자");
        assertThat(voteNode.path("postDate").asText()).isEqualTo(DateUtil.formatBeforeTime(LocalDateTime.now()));

        JsonNode voteItems = voteNode.path("voteItems");
        assertThat(voteItems).hasSize(2);
        assertThat(voteItems.get(0).get("itemName").asText()).isEqualTo("찬성");
        assertThat(voteItems.get(0).get("isVoted").asBoolean()).isEqualTo(false);
        assertThat(voteItems.get(0).get("percentage").asInt()).isEqualTo(75);

        JsonNode recordNode = jsonNode.path("data").path("recordList").get(1);
        assertThat(recordNode.path("type").asText()).isEqualTo("RECORD");
        assertThat(recordNode.path("page").asInt()).isEqualTo(1);
        assertThat(recordNode.path("content").asText()).isEqualTo("레코드 내용");
        assertThat(recordNode.path("nickName").asText()).isEqualTo("사용자");
        assertThat(recordNode.path("postDate").asText()).isEqualTo(DateUtil.formatBeforeTime(LocalDateTime.now()));
        assertThat(recordNode.path("likeCount").asInt()).isEqualTo(1);
        assertThat(recordNode.path("commentCount").asInt()).isEqualTo(2);

    }


}