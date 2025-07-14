package konkuk.thip.user.domain;

import konkuk.thip.user.adapter.out.jpa.UserRoomRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoomParticipantsTest {

    private UserRoom createUserRoom(Long id, Long userId, Long roomId, UserRoomRole role) {
        return UserRoom.builder()
                .id(id)
                .currentPage(0)
                .userPercentage(0.0)
                .userRoomRole(role.getType())
                .userId(userId)
                .roomId(roomId)
                .build();
    }

    @Test
    @DisplayName("해당 모임방에 속한 유저의 수를 반환한다.")
    void calculate_member_count_test() {
        //given
        UserRoom ur1 = createUserRoom(1L, 100L, 10L, UserRoomRole.MEMBER);
        UserRoom ur2 = createUserRoom(2L, 200L, 10L, UserRoomRole.HOST);
        RoomParticipants participants = RoomParticipants.from(List.of(ur1, ur2));

        //when
        int count = participants.calculateMemberCount();

        //then
        assertEquals(2, count);
    }

    @Test
    @DisplayName("유저가 현재 모임방에 속하면 true, 속하지 않으면 false를 반환한다.")
    void is_joining_to_room_test() {
        //given
        UserRoom ur = createUserRoom(1L, 123L, 10L, UserRoomRole.MEMBER);
        RoomParticipants participants = RoomParticipants.from(List.of(ur));

        //when //then
        assertTrue(participants.isJoiningToRoom(123L));
        assertFalse(participants.isJoiningToRoom(999L));
    }

    @Test
    @DisplayName("유저가 현재 모임방의 HOST이면 true, 아니면 false를 반환한다.")
    void is_host_of_room_test() {
        //given
        UserRoom member = createUserRoom(1L, 1L, 5L, UserRoomRole.MEMBER);
        UserRoom host   = createUserRoom(2L, 2L, 5L, UserRoomRole.HOST);
        RoomParticipants participants = RoomParticipants.from(List.of(member, host));

        //when //then
        assertTrue(participants.isHostOfRoom(2L));
        assertFalse(participants.isHostOfRoom(1L));
        assertFalse(participants.isHostOfRoom(3L));
    }
}
