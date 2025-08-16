package konkuk.thip.room.domain;

import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.room.adapter.out.jpa.RoomParticipantRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("[단위] RoomParticipants 도메인 테스트")
class RoomParticipantsTest {

    private RoomParticipant createUserRoom(Long id, Long userId, Long roomId, RoomParticipantRole role) {
        return RoomParticipant.builder()
                .id(id)
                .currentPage(0)
                .userPercentage(0.0)
                .roomParticipantRole(role.getType())
                .userId(userId)
                .roomId(roomId)
                .build();
    }

    @Test
    @DisplayName("해당 모임방에 속한 유저의 수를 반환한다.")
    void calculate_member_count_test() {
        //given
        RoomParticipant ur1 = createUserRoom(1L, 100L, 10L, RoomParticipantRole.MEMBER);
        RoomParticipant ur2 = createUserRoom(2L, 200L, 10L, RoomParticipantRole.HOST);
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
        RoomParticipant ur = createUserRoom(1L, 123L, 10L, RoomParticipantRole.MEMBER);
        RoomParticipants participants = RoomParticipants.from(List.of(ur));

        //when //then
        assertTrue(participants.isJoiningToRoom(123L));
        assertFalse(participants.isJoiningToRoom(999L));
    }

    @Test
    @DisplayName("유저가 현재 모임방의 HOST이면 true, 아니면 false를 반환한다.")
    void is_host_of_room_test() {
        //given
        RoomParticipant member = createUserRoom(1L, 1L, 5L, RoomParticipantRole.MEMBER);
        RoomParticipant host   = createUserRoom(2L, 2L, 5L, RoomParticipantRole.HOST);
        RoomParticipants participants = RoomParticipants.from(List.of(member, host));

        //when //then
        assertTrue(participants.isHostOfRoom(2L));
        assertFalse(participants.isHostOfRoom(1L));
        assertFalse(participants.isHostOfRoom(3L));
    }

    @Test
    @DisplayName("유저가 현재 모임방에서 마지막으로 활동한 페이지(= currentPage) 를 반환한다.")
    void get_current_page_of_user_test() {
        //given
        RoomParticipant ur = createUserRoom(1L, 123L, 10L, RoomParticipantRole.MEMBER);
        RoomParticipants participants = RoomParticipants.from(List.of(ur));

        //when
        int page = participants.getCurrentPageOfUser(123L);

        //then
        assertEquals(0, page);
    }

    @Test
    @DisplayName("현재 모임방에 속하지 않는 유저가 getCurrentPageOfUser 메서드를 호출하면, InvalidStateException이 발생한다.")
    void get_current_page_of_user_not_belong_test() {
        //given
        RoomParticipant ur = createUserRoom(1L, 123L, 10L, RoomParticipantRole.MEMBER);
        RoomParticipants participants = RoomParticipants.from(List.of(ur));

        //when & then
        assertThrows(InvalidStateException.class, () -> participants.getCurrentPageOfUser(999L));
    }

    @Test
    @DisplayName("유저가 현재 모임방에서 활동한 percentage(= userPercentage) 를 반환한다.")
    void get_user_percentage_of_user_test() {
        //given
        RoomParticipant ur = createUserRoom(1L, 123L, 10L, RoomParticipantRole.MEMBER);
        RoomParticipants participants = RoomParticipants.from(List.of(ur));

        //when
        double percentage = participants.getUserPercentageOfUser(123L);

        //then
        assertEquals(0.0, percentage);
    }

    @Test
    @DisplayName("현재 모임방에 속하지 않는 유저가 getUserPercentageOfUser 메서드를 호출하면, InvalidStateException이 발생한다.")
    void get_user_percentage_of_user_not_belong_test() {
        //given
        RoomParticipant ur = createUserRoom(1L, 123L, 10L, RoomParticipantRole.MEMBER);
        RoomParticipants participants = RoomParticipants.from(List.of(ur));

        //when & then
        assertThrows(InvalidStateException.class, () -> participants.getUserPercentageOfUser(999L));
    }
}
