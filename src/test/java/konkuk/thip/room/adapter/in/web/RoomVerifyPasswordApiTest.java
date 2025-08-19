package konkuk.thip.room.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.room.adapter.in.web.request.RoomVerifyPasswordRequest;
import konkuk.thip.room.domain.Category;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.user.domain.Alias;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static konkuk.thip.common.exception.code.ErrorCode.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[통합] 비공개 방 비밀번호 입력 검증 api 통합 테스트")
class RoomVerifyPasswordApiTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private BookJpaRepository bookJpaRepository;
    @Autowired private RoomJpaRepository roomJpaRepository;
    @Autowired private ObjectMapper objectMapper;

    private static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    private Long userId;
    private Long privateRoomId;
    private Long publicRoomId;

    @BeforeEach
    void setUp() {
        Alias alias = TestEntityFactory.createLiteratureAlias();
        UserJpaEntity user = userJpaRepository.save(TestEntityFactory.createUser(alias, "User1"));
        BookJpaEntity book = bookJpaRepository.save(BookJpaEntity.builder()
                .isbn("1234567890123")
                .title("테스트책")
                .imageUrl("https://image.url")
                .authorName("저자")
                .publisher("출판사")
                .description("설명")
                .bestSeller(false)
                .build());
        userId = user.getUserId();

        Category category = TestEntityFactory.createLiteratureCategory();

        // 비공개 방 저장 (비밀번호: 1234)
        RoomJpaEntity privateRoom = roomJpaRepository.save(
                RoomJpaEntity.builder()
                        .bookJpaEntity(book)
                        .category(category)
                        .title("비공개방")
                        .description("비공개방입니다")
                        .isPublic(false)
                        .password(PASSWORD_ENCODER.encode("1234"))
                        .startDate(LocalDate.now().plusDays(1))
                        .endDate(LocalDate.now().plusDays(5))
                        .recruitCount(3)
                        .build()
        );
        privateRoomId = privateRoom.getRoomId();

        // 공개 방 저장
        RoomJpaEntity publicRoom = roomJpaRepository.save(
                RoomJpaEntity.builder()
                        .bookJpaEntity(book)
                        .category(category)
                        .title("공개방")
                        .description("공개방입니다")
                        .isPublic(true)
                        .password(null)
                        .startDate(LocalDate.now().plusDays(1))
                        .endDate(LocalDate.now().plusDays(5))
                        .recruitCount(3)
                        .build()
        );
        publicRoomId = publicRoom.getRoomId();
    }

    @AfterEach
    void tearDown() {
        roomJpaRepository.deleteAll();
        bookJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("모집기간이 만료되지 않은 비공개 방의 비밀번호 입력 검증에 [성공]한다")
    void verifyRoomPassword_success() throws Exception {

        // given
        RoomVerifyPasswordRequest request = new RoomVerifyPasswordRequest("1234");

        //when & then
        mockMvc.perform(post("/rooms/{roomId}/password", privateRoomId)
                        .requestAttr("userId", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data.matched").value(true))
                .andExpect(jsonPath("$.data.roomId").value(privateRoomId));
    }

    @Test
    @DisplayName("모집기간이 만료되지 않은 비공개 방의 비밀번호 입력 검증 [실패]시 matched=false로 200 OK")
    void verifyRoomPassword_mismatch() throws Exception {

        // given
        RoomVerifyPasswordRequest request = new RoomVerifyPasswordRequest("9999");

        //when & then
        mockMvc.perform(post("/rooms/{roomId}/password", privateRoomId)
                        .requestAttr("userId", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data.matched").value(false))
                .andExpect(jsonPath("$.data.roomId").value(privateRoomId));
    }

    @Test
    @DisplayName("[모집기간이 만료된] 비공개 방에 비밀번호 입력 검증 시 400 Bad Request 반환")
    void verifyRoomPassword_recruitmentPeriodExpired() throws Exception {

        // given
        // 모집기간: startDate.minusDays(1) 이전이면 만료
        // startDate를 오늘로 설정해 모집기간이 이미 지난 상태로 저장
        RoomJpaEntity expiredRoom = roomJpaRepository.save(
                RoomJpaEntity.builder()
                        .bookJpaEntity(bookJpaRepository.findAll().get(0))
                        .category(Category.LITERATURE)
                        .title("모집만료방")
                        .description("모집기간이 만료된 방입니다")
                        .isPublic(false)
                        .password(PASSWORD_ENCODER.encode("1234"))
                        .startDate(LocalDate.now()) // 오늘 시작이므로 모집기간은 어제까지
                        .endDate(LocalDate.now().plusDays(5))
                        .recruitCount(3)
                        .build()
        );
        Long expiredRoomId = expiredRoom.getRoomId();

        RoomVerifyPasswordRequest request = new RoomVerifyPasswordRequest("1234");

        //when & then
        mockMvc.perform(post("/rooms/{roomId}/password", expiredRoomId)
                        .requestAttr("userId", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value(ROOM_RECRUITMENT_PERIOD_EXPIRED.getCode()))
                .andExpect(jsonPath("$.message", containsString(("모집기간이 만료된 방입니다."))));
    }

    @Test
    @DisplayName("[공개방]에 비밀번호 입력 검증 시 400 Bad Request 반환")
    void verifyRoomPassword_publicRoom() throws Exception {

        // given
        RoomVerifyPasswordRequest request = new RoomVerifyPasswordRequest("1234");

        //when & then
        mockMvc.perform(post("/rooms/{roomId}/password", publicRoomId)
                        .requestAttr("userId", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value(ROOM_PASSWORD_NOT_REQUIRED.getCode()))
                .andExpect(jsonPath("$.message", containsString("공개방은 비밀번호가 필요하지 않습니다")));
    }
}
