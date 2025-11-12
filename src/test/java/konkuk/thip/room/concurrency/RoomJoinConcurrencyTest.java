package konkuk.thip.room.concurrency;

import com.fasterxml.jackson.databind.ObjectMapper;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.room.adapter.in.web.request.RoomJoinRequest;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.roomparticipant.RoomParticipantJpaRepository;
import konkuk.thip.room.domain.value.Category;
import konkuk.thip.room.domain.value.RoomParticipantRole;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.domain.value.Alias;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[동시성] 방 참여 동시 요청 테스트")
@Tag("concurrency")
public class RoomJoinConcurrencyTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private RoomJpaRepository roomJpaRepository;
    @Autowired private RoomParticipantJpaRepository roomParticipantJpaRepository;
    @Autowired private BookJpaRepository bookJpaRepository;
    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("[동시성] 방 참여 동시 요청 테스트")
    void room_join_test_in_multi_thread() throws Exception {
        //given
        int requestUserCount = 500;

        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook());
        // 모집인원 10명 방 생성
        RoomJpaEntity room = roomJpaRepository.save(TestEntityFactory.createCustomRoom(book, Category.LITERATURE, 10));
        List<Long> savedUserIds = createUsersRange(requestUserCount + 1);

        UserJpaEntity hostUser = userJpaRepository.save(TestEntityFactory.createUser(Alias.WRITER));
        roomParticipantJpaRepository.save(TestEntityFactory.createRoomParticipant(room, hostUser, RoomParticipantRole.HOST, 0.0));

        List<Long> requestUserIds = savedUserIds.subList(1, savedUserIds.size());

        //when : 동시에 방 참여 요청
        ExecutorService pool = Executors.newFixedThreadPool(Math.min(requestUserCount, 100));
        CountDownLatch ready = new CountDownLatch(requestUserCount);
        CountDownLatch start = new  CountDownLatch(1);
        CountDownLatch finish = new  CountDownLatch(requestUserCount);

        RoomJoinRequest body = new RoomJoinRequest("join");
        String json = objectMapper.writeValueAsString(body);

        List<Future<Integer>> results = new ArrayList<>(requestUserCount);
        for (int i = 0; i < requestUserCount; i++) {
            final long userId = requestUserIds.get(i);
            results.add(pool.submit(() -> {
                ready.countDown();
                start.await();
                try {
                    mockMvc.perform(post("/rooms/{roomId}/join", room.getRoomId())
                                    .contentType("application/json")
                                    .content(json)
                                    .requestAttr("userId", userId))
                            .andExpect(status().isOk());
                    return 200;
                } catch (AssertionError e) {
                    return 400;
                } finally {
                    finish.countDown();
                }
            }));
        }

        // 동시에 시작
        ready.await(10, TimeUnit.SECONDS);
        start.countDown();
        finish.await(60, TimeUnit.SECONDS);
        pool.shutdown();

        long okCount = results.stream().filter(f -> {
            try {
                return f.get() == 200;
            } catch (Exception e) {
                return false;
            }
        }).count();

        //then : DB 실측값 검증
        RoomJpaEntity reloadedRoom = roomJpaRepository.findByRoomId(room.getRoomId()).orElseThrow();

        long participantRows = jdbcTemplate.query(
                "SELECT COUNT(*) FROM room_participants WHERE room_id = ?",
                ps -> ps.setLong(1, room.getRoomId()),
                rs -> {rs.next(); return rs.getLong(1); }
        );

        int memberCountInRoom = reloadedRoom.getMemberCount();
        int recruit = reloadedRoom.getRecruitCount();

        System.out.println("=== RESULT ===");
        System.out.println("OK responses(= 방 참여 OK 응답을 받은 thread 수) : " + okCount);
        System.out.println("participants rows(= host 1명 포함된 결과)      : " + participantRows);
        System.out.println("room.memberCount                            : " + memberCountInRoom);
        System.out.println("recruitCount                                : " + recruit);

        /**
         * 현재 방 참여 로직
         * 1. 방 참여자에 해당하는 RoomParticipant 엔티티 저장
         * 2. Room 엔티티의 memberCount 증가 (Room의 도메인 규칙으로 memberCount가 recruitCount를 초과하지 않도록 제한됨)
         * -> 동시성 경쟁이 발생하는 트래픽 규모에서는 participants 테이블의 행 수와 Room.memberCount 동기화 보장 X
         */

        // 1) participants가 recruitCount 보다 커질 수 있음
        assertThat(participantRows).isGreaterThan(recruit);

        // 2) memberCount가 실제 participants 수보다 작을 수 있음
        // memberCount 값은 Room 도메인 규칙에 의해 recruitCount를 초과하여 증가하지 않음
        assertThat(memberCountInRoom).isLessThan((int) participantRows);
    }

    private List<Long> createUsersRange(long count) {
        List<Long> userIds = new ArrayList<>();
        for (long i = 0; i < count; i++) {
            UserJpaEntity saved = userJpaRepository.save(TestEntityFactory.createUser(Alias.WRITER));
            userIds.add(saved.getUserId());
        }
        return userIds;
    }
}
