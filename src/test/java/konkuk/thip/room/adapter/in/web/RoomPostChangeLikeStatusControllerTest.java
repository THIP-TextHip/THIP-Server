package konkuk.thip.room.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.room.domain.value.Category;
import konkuk.thip.roompost.adapter.out.jpa.RecordJpaEntity;
import konkuk.thip.roompost.adapter.out.persistence.repository.record.RecordJpaRepository;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.domain.value.RoomParticipantRole;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.roomparticipant.RoomParticipantJpaRepository;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.domain.value.Alias;
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

import java.util.HashMap;
import java.util.Map;

import static konkuk.thip.common.exception.code.ErrorCode.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@DisplayName("[단위] 방 게시물(기록,투표) 좋아요 api controller 단위 테스트")
class RoomPostChangeLikeStatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private BookJpaRepository bookJpaRepository;
    @Autowired private RoomJpaRepository roomJpaRepository;
    @Autowired private RoomParticipantJpaRepository roomParticipantJpaRepository;
    @Autowired private RecordJpaRepository recordJpaRepository;

    private UserJpaEntity user1;
    private UserJpaEntity user2;
    private BookJpaEntity book;
    private Category category;
    private RoomJpaEntity room;
    private RecordJpaEntity record;

    private static final String ROOM_POST_LIKE_API_PATH = "/room-posts/{postId}/likes";

    @BeforeEach
    void setUp() {
        Alias alias = TestEntityFactory.createLiteratureAlias();
        user1 = userJpaRepository.save(TestEntityFactory.createUser(alias));
        user2 = userJpaRepository.save(TestEntityFactory.createUser(alias));
        book = bookJpaRepository.save(TestEntityFactory.createBookWithISBN("9788954682152"));
        category = TestEntityFactory.createLiteratureCategory();
        room = roomJpaRepository.save(TestEntityFactory.createRoom(book, category));
        // 1번방에 유저 1이 호스트
        roomParticipantJpaRepository.save(TestEntityFactory.createRoomParticipant(room,user1, RoomParticipantRole.HOST, 80.0));
        record = recordJpaRepository.save(TestEntityFactory.createRecord(user1,room));
    }

    private Map<String, Object> buildValidLikeRequest(Boolean isLike, String postType) {
        Map<String, Object> request = new HashMap<>();
        request.put("type", isLike);
        request.put("roomPostType", postType);
        return request;
    }

    private void assertBadRequest(int expectedCode, Map<String, Object> request, String message) throws Exception {
        mockMvc.perform(post(ROOM_POST_LIKE_API_PATH, record.getPostId())
                        .requestAttr("userId", user1.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(expectedCode))
                .andExpect(jsonPath("$.message", containsString(message)));
    }

    @Test
    @DisplayName("잘못된 RoomPostType 값이 들어오면 400 Bad Request 반환")
    void invalidPostType_shouldReturnBadRequest() throws Exception {
        Map<String, Object> req = buildValidLikeRequest(true,"FEED");
        assertBadRequest(ROOM_POST_TYPE_NOT_MATCH.getCode(), req, "일치하는 방 게시물 타입 이름이 없습니다.");
    }

    @Test
    @DisplayName("방 참여자가 아닌 사용자가 방 게시물에 좋아요 하려고 하면 400 Bad Request 반환")
    void nonParticipantUser_likeRoomPost_shouldReturnBadRequest() throws Exception {
        Map<String, Object> req = buildValidLikeRequest(true, "RECORD");

        mockMvc.perform(post(ROOM_POST_LIKE_API_PATH, record.getPostId())
                        .requestAttr("userId", user2.getUserId())  // 방 참여하지 않은 user2로 요청
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(req))
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ROOM_ACCESS_FORBIDDEN.getCode()))
                .andExpect(jsonPath("$.message", containsString("사용자가 이 방의 참가자가 아닙니다.")));
    }
}
