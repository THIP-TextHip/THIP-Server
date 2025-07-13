package konkuk.thip.room.application.service;

import konkuk.thip.room.adapter.in.web.response.RoomGetMemberListResponse;
import konkuk.thip.room.application.port.in.RoomGetMemberListUseCase;
import konkuk.thip.room.application.port.out.RoomCommandPort;
import konkuk.thip.room.domain.Room;
import konkuk.thip.user.application.port.out.FollowingQueryPort;
import konkuk.thip.user.application.port.out.UserCommandPort;
import konkuk.thip.user.application.port.out.UserRoomCommandPort;
import konkuk.thip.user.domain.User;
import konkuk.thip.user.domain.UserRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomGetMemberListService implements RoomGetMemberListUseCase {

    private final RoomCommandPort roomCommandPort;
    private final UserRoomCommandPort userRoomCommandPort;
    private final UserCommandPort userCommandPort;
    private final FollowingQueryPort followingQueryPort;

    @Override
    @Transactional(readOnly = true)
    public RoomGetMemberListResponse getRoomMemberList(Long roomId) {

        // 1. 방 검증 및 방 조회
        Room room = roomCommandPort.findById(roomId);

        // 2. 방 참여자(UserRoom) 전체 조회
        List<UserRoom> userRooms = userRoomCommandPort.findAllByRoomId(room.getId());

        // 3. 각 참여자의 userId로 유저정보, 구독자 수(팔로워 수) 조회
        List<RoomGetMemberListResponse.MemberSearchResult> userList = userRooms.stream()
                .map(userRoom -> {

                    Long memberUserId = userRoom.getUserId();
                    // 팔로워 수 = Following 테이블에서 followingUserId == memberUserId 인 row 개수
                    int subscriberCount = followingQueryPort.countByFollowingUserId(memberUserId);
                    // 유저 정보 조회
                    User user = userCommandPort.findById(memberUserId);

                    return RoomGetMemberListResponse.MemberSearchResult.builder()
                            .userId(memberUserId)
                            .nickname(user.getNickname())
                            .imageUrl(user.getAlias().getImageUrl())
                            .alias(user.getAlias().getValue())
                            .subscriberCount(subscriberCount)
                            .build();
                })
                .toList();

        // 4. DTO 조립 후 반환
        return RoomGetMemberListResponse.builder()
                .userList(userList)
                .build();
    }


}
