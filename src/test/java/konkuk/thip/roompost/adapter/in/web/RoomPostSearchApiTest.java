package konkuk.thip.roompost.adapter.in.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.room.domain.value.Category;
import konkuk.thip.roompost.adapter.out.jpa.RecordJpaEntity;
import konkuk.thip.roompost.adapter.out.persistence.repository.record.RecordJpaRepository;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomParticipantJpaEntity;
import konkuk.thip.room.domain.value.RoomParticipantRole;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.roomparticipant.RoomParticipantJpaRepository;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.domain.value.UserRole;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.roompost.adapter.out.jpa.VoteItemJpaEntity;
import konkuk.thip.roompost.adapter.out.jpa.VoteJpaEntity;
import konkuk.thip.roompost.adapter.out.persistence.repository.vote.VoteItemJpaRepository;
import konkuk.thip.roompost.adapter.out.persistence.repository.vote.VoteJpaRepository;
import konkuk.thip.user.domain.value.Alias;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("[통합] 게시글 조회 api 테스트")
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class RoomPostSearchApiTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private VoteJpaRepository voteJpaRepository;
    @Autowired private VoteItemJpaRepository voteItemJpaRepository;
    @Autowired private RecordJpaRepository recordJpaRepository;
    @Autowired private RoomJpaRepository roomJpaRepository;
    @Autowired private RoomParticipantJpaRepository roomParticipantJpaRepository;
    @Autowired private BookJpaRepository bookJpaRepository;
    @Autowired private UserJpaRepository userJpaRepository;

    @Test
    @DisplayName("그룹 기록 조회 - 기본 조회 성공")
    void searchGroupRecords_basic_success() throws Exception {
        // given
        TestData testData = createTestData();

        // when
        ResultActions result = mockMvc.perform(get("/rooms/" + testData.room.getRoomId() + "/posts")
                .requestAttr("userId", testData.user.getUserId())
                .param("type", "group")
                .param("sort", "latest")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data.postList").isNotEmpty())
                .andExpect(jsonPath("$.data.isLast").isBoolean());

        String json = result.andReturn().getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(json);
        JsonNode postList = jsonNode.path("data").path("postList");
        boolean isLast = jsonNode.path("data").path("isLast").asBoolean();
        JsonNode nextCursor = jsonNode.path("data").path("nextCursor");

        assertThat(postList.isEmpty()).isFalse();
        if(isLast) {
            assertThat(nextCursor.isNull()).isTrue();
        } else {
            assertThat(nextCursor.isTextual()).isTrue();
        }

    }

    @Test
    @DisplayName("그룹 기록 조회 - 투표와 기록 모두 포함")
    void searchGroupRecords_with_vote_and_record_success() throws Exception {
        // given
        TestData testData = createTestDataWithVote();

        // when
        ResultActions result = mockMvc.perform(get("/rooms/" + testData.room.getRoomId() + "/posts")
                .requestAttr("userId", testData.user.getUserId())
                .param("type", "group")
                .param("sort", "latest")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk());

        String json = result.andReturn().getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(json);
        JsonNode postList = jsonNode.path("data").path("postList");

        // 투표와 기록이 모두 포함되어 있는지 확인
        boolean hasRecord = false;
        boolean hasVote = false;

        for (JsonNode post : postList) {
            String postType = post.path("postType").asText();
            if ("RECORD".equals(postType)) {
                hasRecord = true;
                assertThat(post.path("voteItems")).isEmpty();
            } else if ("VOTE".equals(postType)) {
                hasVote = true;
                assertThat(post.path("voteItems")).isNotEmpty();
                assertThat(post.path("voteItems").size()).isGreaterThan(0);

                // 투표 항목 검증
                JsonNode voteItems = post.path("voteItems");
                for (JsonNode voteItem : voteItems) {
                    assertThat(voteItem.path("voteItemId")).isNotNull();
                    assertThat(voteItem.path("itemName")).isNotNull();
                    assertThat(voteItem.path("percentage")).isNotNull();
                    assertThat(voteItem.path("isVoted")).isNotNull();
                }
            }
        }

        assertThat(hasRecord).isTrue();
        assertThat(hasVote).isTrue();
    }

    @Test
    @DisplayName("내 기록 조회 성공")
    void searchMyRecords_success() throws Exception {
        // given
        TestData testData = createTestData();

        // when
        ResultActions result = mockMvc.perform(get("/rooms/" + testData.room.getRoomId() + "/posts")
                .requestAttr("userId", testData.user.getUserId())
                .param("type", "mine")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk());

        String json = result.andReturn().getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(json);
        JsonNode postList = jsonNode.path("data").path("postList");

        // 모든 게시물이 요청한 사용자의 것인지 확인
        for (JsonNode post : postList) {
            assertThat(post.path("userId").asLong()).isEqualTo(testData.user.getUserId());
            assertThat(post.path("isWriter").asBoolean()).isTrue();
        }
    }

    @Test
    @DisplayName("페이지 필터 적용 조회 성공")
    void searchGroupRecords_with_page_filter_success() throws Exception {
        // given
        TestData testData = createTestData();

        // when
        ResultActions result = mockMvc.perform(get("/rooms/" + testData.room.getRoomId() + "/posts")
                .requestAttr("userId", testData.user.getUserId())
                .param("type", "group")
                .param("sort", "latest")
                .param("pageStart", "1")
                .param("pageEnd", "5")
                .param("isPageFilter", "true")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk());

        String json = result.andReturn().getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(json);
        JsonNode postList = jsonNode.path("data").path("postList");

        // 페이지 범위 내의 게시물만 조회되는지 확인
        for (JsonNode post : postList) {
            int page = post.path("page").asInt();
            assertThat(page).isBetween(1, 5);
        }
    }

    @Test
    @DisplayName("총평보기 필터 적용 조회 성공")
    void searchGroupRecords_with_overview_filter_success() throws Exception {
        // given
        TestData testData = createTestDataWithOverview();

        // when
        ResultActions result = mockMvc.perform(get("/rooms/" + testData.room.getRoomId() + "/posts")
                .requestAttr("userId", testData.user.getUserId())
                .param("type", "group")
                .param("sort", "latest")
                .param("isOverview", "true")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk());

        String json = result.andReturn().getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(json);
        JsonNode postList = jsonNode.path("data").path("postList");

        // 모든 게시물이 총평인지 확인
        for (JsonNode post : postList) {
            int page = post.path("page").asInt();
            assertThat(page).isEqualTo(testData.book.getPageCount());
        }
    }

    @Test
    @DisplayName("좋아요순 정렬 조회 성공")
    void searchGroupRecords_sort_by_like_success() throws Exception {
        // given
        TestData testData = createTestData();

        // when
        ResultActions result = mockMvc.perform(get("/rooms/" + testData.room.getRoomId() + "/posts")
                .requestAttr("userId", testData.user.getUserId())
                .param("type", "group")
                .param("sort", "like")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk());

        String json = result.andReturn().getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(json);
        JsonNode postList = jsonNode.path("data").path("postList");

        // 좋아요 수 내림차순 정렬 확인
        int prevLikeCount = Integer.MAX_VALUE;
        for (JsonNode post : postList) {
            int currentLikeCount = post.path("likeCount").asInt();
            assertThat(currentLikeCount).isLessThanOrEqualTo(prevLikeCount);
            prevLikeCount = currentLikeCount;
        }
    }

    @Test
    @DisplayName("댓글순 정렬 조회 성공")
    void searchGroupRecords_sort_by_comment_success() throws Exception {
        // given
        TestData testData = createTestData();

        // when
        ResultActions result = mockMvc.perform(get("/rooms/" + testData.room.getRoomId() + "/posts")
                .requestAttr("userId", testData.user.getUserId())
                .param("type", "group")
                .param("sort", "comment")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk());

        String json = result.andReturn().getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(json);
        JsonNode postList = jsonNode.path("data").path("postList");

        // 댓글 수 내림차순 정렬 확인
        int prevCommentCount = Integer.MAX_VALUE;
        for (JsonNode post : postList) {
            int currentCommentCount = post.path("commentCount").asInt();
            assertThat(currentCommentCount).isLessThanOrEqualTo(prevCommentCount);
            prevCommentCount = currentCommentCount;
        }
    }

    @Test
    @DisplayName("커서 기반 페이징 동작 확인")
    void searchGroupRecords_cursor_paging_success() throws Exception {
        // given
        TestData testData = createTestData();

        // 첫 번째 페이지 조회
        ResultActions firstResult = mockMvc.perform(get("/rooms/" + testData.room.getRoomId() + "/posts")
                .requestAttr("userId", testData.user.getUserId())
                .param("type", "group")
                .param("sort", "latest")
                .contentType(MediaType.APPLICATION_JSON));

        String firstJson = firstResult.andReturn().getResponse().getContentAsString();
        JsonNode firstJsonNode = objectMapper.readTree(firstJson);
        String nextCursor = firstJsonNode.path("data").path("nextCursor").asText();
        boolean isLast = firstJsonNode.path("data").path("isLast").asBoolean();

        if (!isLast && !nextCursor.isEmpty()) {
            // when - 다음 페이지 조회
            ResultActions secondResult = mockMvc.perform(get("/rooms/" + testData.room.getRoomId() + "/posts")
                    .requestAttr("userId", testData.user.getUserId())
                    .param("type", "group")
                    .param("sort", "latest")
                    .param("cursor", nextCursor)
                    .contentType(MediaType.APPLICATION_JSON));

            // then
            secondResult.andExpect(status().isOk());

            String secondJson = secondResult.andReturn().getResponse().getContentAsString();
            JsonNode secondJsonNode = objectMapper.readTree(secondJson);
            JsonNode secondPostList = secondJsonNode.path("data").path("postList");

            // 두 번째 페이지도 결과가 있는지 확인
            assertThat(secondPostList.isEmpty()).isFalse();
        }
    }

    @Test
    @DisplayName("잠긴 게시물 블러 처리 확인")
    void searchGroupRecords_locked_content_blurred() throws Exception {
        // given
        TestData testData = createTestDataWithLockedContent();

        // when
        ResultActions result = mockMvc.perform(get("/rooms/" + testData.room.getRoomId() + "/posts")
                .requestAttr("userId", testData.user.getUserId())
                .param("type", "group")
                .param("sort", "latest")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk());

        String json = result.andReturn().getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(json);
        JsonNode postList = jsonNode.path("data").path("postList");

        for (JsonNode post : postList) {
            boolean isLocked = post.path("isLocked").asBoolean();
            String content = post.path("content").asText();

            if (isLocked) {
                assertThat(content).contains("여긴 못 지나가지롱~~");
            }
        }
    }

    @Test
    @DisplayName("사용자가 방에 속하지 않는 경우 오류 반환")
    void searchRecords_user_not_in_room_error() throws Exception {
        // given
        TestData testData = createTestData();
        Long nonParticipantUserId = 99999L;

        // when & then
        mockMvc.perform(get("/rooms/" + testData.room.getRoomId() + "/posts")
                        .requestAttr("userId", nonParticipantUserId)
                        .param("type", "group")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("잘못된 파라미터로 조회 시 오류 반환")
    void searchRecords_invalid_parameters_error() throws Exception {
        // given
        TestData testData = createTestData();

        // when & then - 페이지 필터와 총평보기 동시 적용
        mockMvc.perform(get("/rooms/" + testData.room.getRoomId() + "/posts")
                        .requestAttr("userId", testData.user.getUserId())
                        .param("type", "group")
                        .param("pageStart", "1")
                        .param("pageEnd", "10")
                        .param("isPageFilter", "true")
                        .param("isOverview", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("내 기록 조회 시 불필요한 파라미터 포함하면 오류 반환")
    void searchMyRecords_with_invalid_parameters_error() throws Exception {
        // given
        TestData testData = createTestData();

        // when & then
        mockMvc.perform(get("/rooms/" + testData.room.getRoomId() + "/posts")
                        .requestAttr("userId", testData.user.getUserId())
                        .param("type", "mine")
                        .param("sort", "latest") // 내 기록 조회에서는 sort 파라미터가 null이어야 함
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    private TestData createTestData() {
        Alias alias = TestEntityFactory.createLiteratureAlias();

        UserJpaEntity user = userJpaRepository.save(UserJpaEntity.builder()
                .oauth2Id("kakao_123")
                .nickname("테스트사용자")
                .nicknameUpdatedAt(LocalDate.now().minusMonths(7))
                .role(UserRole.USER)
                .alias(alias)
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

        Category category = TestEntityFactory.createLiteratureCategory();

        RoomJpaEntity room = roomJpaRepository.save(RoomJpaEntity.builder()
                .title("방 제목")
                .description("설명")
                .isPublic(true)
                .roomPercentage(0.0)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(30))
                .recruitCount(5)
                .bookJpaEntity(book)
                .category(category)
                .build());

        // 방 참가자 생성
        roomParticipantJpaRepository.save(RoomParticipantJpaEntity.builder()
                .userJpaEntity(user)
                .roomJpaEntity(room)
                .currentPage(50)
                .userPercentage(25.0)
                .roomParticipantRole(RoomParticipantRole.MEMBER)
                .build());

        // 기록 생성
        for (int i = 1; i <= 5; i++) {
            recordJpaRepository.save(RecordJpaEntity.builder()
                    .userJpaEntity(user)
                    .roomJpaEntity(room)
                    .likeCount(i * 2)
                    .commentCount(i)
                    .page(i * 10)
                    .content("기록 내용 " + i)
                    .isOverview(false)
                    .build());
        }

        return new TestData(user, book, room);
    }

    private TestData createTestDataWithVote() {
        TestData testData = createTestData();

        // 투표 게시물 생성
        VoteJpaEntity vote = voteJpaRepository.save(VoteJpaEntity.builder()
                .userJpaEntity(testData.user)
                .roomJpaEntity(testData.room)
                .likeCount(5)
                .commentCount(3)
                .page(30)
                .content("투표 내용")
                .isOverview(false)
                .build());

        // 투표 항목 생성
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

        return testData;
    }

    private TestData createTestDataWithOverview() {
        TestData testData = createTestData();

        // 총평 기록 생성
        recordJpaRepository.save(RecordJpaEntity.builder()
                .userJpaEntity(testData.user)
                .roomJpaEntity(testData.room)
                .likeCount(10)
                .commentCount(5)
                .page(testData.book.getPageCount())
                .content("총평 내용")
                .isOverview(true)
                .build());

        // 사용자 진행률을 80% 이상으로 업데이트
        RoomParticipantJpaEntity participant = roomParticipantJpaRepository
                .findByUserIdAndRoomId(testData.user.getUserId(), testData.room.getRoomId())
                .orElseThrow();

        participant.updateCurrentPage(180); // 90% 진행
        participant.updateUserPercentage(90.0);

        roomParticipantJpaRepository.save(participant);

        return testData;
    }

    private TestData createTestDataWithLockedContent() {
        Alias alias = TestEntityFactory.createLiteratureAlias();

        UserJpaEntity user = userJpaRepository.save(UserJpaEntity.builder()
                .oauth2Id("kakao_123")
                .nickname("테스트사용자")
                .nicknameUpdatedAt(LocalDate.now().minusMonths(7))
                .role(UserRole.USER)
                .alias(alias)
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

        Category category = TestEntityFactory.createLiteratureCategory();

        RoomJpaEntity room = roomJpaRepository.save(RoomJpaEntity.builder()
                .title("방 제목")
                .description("설명")
                .isPublic(true)
                .roomPercentage(0.0)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(30))
                .recruitCount(5)
                .bookJpaEntity(book)
                .category(category)
                .build());

        // 방 참가자 생성 (현재 페이지 10으로 설정)
        roomParticipantJpaRepository.save(RoomParticipantJpaEntity.builder()
                .userJpaEntity(user)
                .roomJpaEntity(room)
                .currentPage(10)
                .userPercentage(5.0)
                .roomParticipantRole(RoomParticipantRole.MEMBER)
                .build());

        // 잠긴 기록 생성 (현재 페이지보다 높은 페이지)
        recordJpaRepository.save(RecordJpaEntity.builder()
                .userJpaEntity(user)
                .roomJpaEntity(room)
                .likeCount(5)
                .commentCount(2)
                .page(50) // 현재 페이지(10)보다 높음
                .content("잠긴 내용입니다")
                .isOverview(false)
                .build());

        // 잠기지 않은 기록 생성
        recordJpaRepository.save(RecordJpaEntity.builder()
                .userJpaEntity(user)
                .roomJpaEntity(room)
                .likeCount(3)
                .commentCount(1)
                .page(5) // 현재 페이지(10)보다 낮음
                .content("잠기지 않은 내용입니다")
                .isOverview(false)
                .build());

        return new TestData(user, book, room);
    }

    private static class TestData {
        final UserJpaEntity user;
        final BookJpaEntity book;
        final RoomJpaEntity room;

        TestData(UserJpaEntity user, BookJpaEntity book, RoomJpaEntity room) {
            this.user = user;
            this.book = book;
            this.room = room;
        }
    }
}