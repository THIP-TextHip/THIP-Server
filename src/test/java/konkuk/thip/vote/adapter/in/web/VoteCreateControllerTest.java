package konkuk.thip.vote.adapter.in.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.room.adapter.out.jpa.CategoryJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.adapter.out.persistence.repository.CategoryJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.user.adapter.out.jpa.AliasJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserRole;
import konkuk.thip.user.adapter.out.persistence.repository.AliasJpaRepository;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;


import static konkuk.thip.common.exception.code.ErrorCode.API_INVALID_PARAM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[통합] VoteCreateController 테스트")
class VoteCreateControllerTest {

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
    private VoteJpaRepository voteJpaRepository;

    @Autowired
    private VoteItemJpaRepository voteItemJpaRepository;

    @AfterEach
    void tearDown() {
        voteItemJpaRepository.deleteAll();
        voteJpaRepository.deleteAll();
        roomJpaRepository.deleteAll();
        bookJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
        categoryJpaRepository.deleteAll();
        aliasJpaRepository.deleteAll();
    }

    private void saveUserAndRoom() {
        AliasJpaEntity alias = aliasJpaRepository.save(AliasJpaEntity.builder()
                .value("책벌레")
                .color("blue")
                .imageUrl("http://image.url")
                .build());

        UserJpaEntity user = userJpaRepository.save(UserJpaEntity.builder()
                .oauth2Id("kakao_432708231")
                .nickname("User1")
                .imageUrl("https://avatar1.jpg")
                .role(UserRole.USER)
                .aliasForUserJpaEntity(alias)
                .build());

        BookJpaEntity book = bookJpaRepository.save(BookJpaEntity.builder()
                .title("작별하지 않는다")
                .isbn("9788954682152")
                .authorName("한강")
                .bestSeller(false)
                .publisher("문학동네")
                .imageUrl("https://image1.jpg")
                .pageCount(300)
                .description("한강의 소설")
                .build());


        CategoryJpaEntity category = categoryJpaRepository.save(CategoryJpaEntity.builder()
                .value("과학/IT")
                .imageUrl("과학/IT_image")
                .aliasForCategoryJpaEntity(alias)
                .build());

        RoomJpaEntity room = roomJpaRepository.save(RoomJpaEntity.builder()
                .title("한강 독서모임")
                .description("한강 작품 읽기 모임")
                .isPublic(true)
                .roomPercentage(0.0)
                .startDate(LocalDate.now().plusDays(2))
                .endDate(LocalDate.now().plusDays(30))
                .recruitCount(10)
                .bookJpaEntity(book)
                .categoryJpaEntity(category)
                .build());
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

    @Test
    @DisplayName("[page]가 누락되었을 때 400 Bad Request 반환")
    void vote_create_page_null() throws Exception {
        // given: page 누락
        Map<String, Object> request = Map.of(
                "isOverview", false,
                "content", "내용",
                "voteItemList", List.of(Map.of("itemName", "찬성"))
        );

        // when & then
        mockMvc.perform(post("/rooms/{roomId}/vote", 1L)
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(API_INVALID_PARAM.getCode()))
                .andExpect(jsonPath("$.message", containsString("page는 필수입니다.")));
    }

    @Test
    @DisplayName("[isOverview]가 누락되었을 때 400 Bad Request 반환")
    void vote_create_is_over_view_null() throws Exception {
        // given: isOverview 누락
        Map<String, Object> request = Map.of(
                "page", 1,
                "content", "내용",
                "voteItemList", List.of(Map.of("itemName", "찬성"))
        );

        // when & then
        mockMvc.perform(post("/rooms/{roomId}/vote", 1L)
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(API_INVALID_PARAM.getCode()))
                .andExpect(jsonPath("$.message", containsString("isOverview(= 총평 여부)는 필수입니다.")));
    }

    @Test
    @DisplayName("[content]가 빈 문자열일 때 400 Bad Request 반환")
    void vote_create_content_blank() throws Exception {
        // given
        Map<String, Object> request = Map.of(
                "page", 1,
                "isOverview", false,
                "content", "",
                "voteItemList", List.of(Map.of("itemName", "찬성"))
        );

        // when & then
        mockMvc.perform(post("/rooms/{roomId}/vote", 1L)
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(API_INVALID_PARAM.getCode()))
                .andExpect(jsonPath("$.message", containsString("투표 내용은 필수입니다.")));
    }

    @Test
    @DisplayName("[content]가 20자 초과일 때 400 Bad Request 반환")
    void vote_create_content_too_long() throws Exception {
        // given
        String longContent = "가".repeat(21);
        Map<String, Object> request = Map.of(
                "page", 1,
                "isOverview", false,
                "content", longContent,
                "voteItemList", List.of(Map.of("itemName", "찬성"))
        );

        // when & then
        mockMvc.perform(post("/rooms/{roomId}/vote", 1L)
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(API_INVALID_PARAM.getCode()))
                .andExpect(jsonPath("$.message", containsString("투표 내용은 최대 20자 입니다.")));
    }

    @Test
    @DisplayName("[voteItemList]가 누락되었을 때 400 Bad Request 반환")
    void vote_create_vote_item_null() throws Exception {
        // given
        Map<String, Object> request = Map.of(
                "page", 1,
                "isOverview", false,
                "content", "내용"
                // voteItemList 생략
        );

        // when & then
        mockMvc.perform(post("/rooms/{roomId}/vote", 1L)
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(API_INVALID_PARAM.getCode()))
                .andExpect(jsonPath("$.message", containsString("투표 항목은 필수입니다.")));
    }

    @Test
    @DisplayName("[voteItemList]가 5개 초과일 때 400 Bad Request 반환")
    void vote_create_vote_item_too_many() throws Exception {
        // given: 6개 아이템
        List<Map<String, String>> items = List.of(
                Map.of("itemName","A"), Map.of("itemName","B"),
                Map.of("itemName","C"), Map.of("itemName","D"),
                Map.of("itemName","E"), Map.of("itemName","F")
        );
        Map<String, Object> request = Map.of(
                "page", 1,
                "isOverview", false,
                "content", "내용",
                "voteItemList", items
        );

        // when & then
        mockMvc.perform(post("/rooms/{roomId}/vote", 1L)
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(API_INVALID_PARAM.getCode()))
                .andExpect(jsonPath("$.message", containsString("투표 항목은 1개 이상, 최대 5개까지입니다.")));
    }

    @Test
    @DisplayName("[voteItemList] 내 [itemName]이 빈 문자열일 때 400 Bad Request 반환")
    void vote_create_vote_item_name_blank() throws Exception {
        // given
        Map<String, Object> request = Map.of(
                "page", 1,
                "isOverview", false,
                "content", "내용",
                "voteItemList", List.of(Map.of("itemName",""))
        );

        // when & then
        mockMvc.perform(post("/rooms/{roomId}/vote", 1L)
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(API_INVALID_PARAM.getCode()))
                .andExpect(jsonPath("$.message", containsString("투표 항목 이름은 필수입니다.")));
    }

    @Test
    @DisplayName("[voteItemList] 내 [itemName]이 20자 초과일 때 400 Bad Request 반환")
    void vote_create_vote_item_name_too_long() throws Exception {
        // given
        String longName = "가".repeat(21);
        Map<String, Object> request = Map.of(
                "page", 1,
                "isOverview", false,
                "content", "내용",
                "voteItemList", List.of(Map.of("itemName", longName))
        );

        // when & then
        mockMvc.perform(post("/rooms/{roomId}/vote", 1L)
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(API_INVALID_PARAM.getCode()))
                .andExpect(jsonPath("$.message", containsString("투표 항목 이름은 최대 20자입니다.")));
    }
}