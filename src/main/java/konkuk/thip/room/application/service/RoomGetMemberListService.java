package konkuk.thip.room.application.service;

import konkuk.thip.room.adapter.in.web.response.RoomGetMemberListResponse;
import konkuk.thip.room.application.port.in.RoomGetMemberListUseCase;
import konkuk.thip.room.application.port.out.RoomCommandPort;
import konkuk.thip.room.domain.Room;
import konkuk.thip.user.application.port.out.FollowingQueryPort;
import konkuk.thip.user.application.port.out.UserCommandPort;
import konkuk.thip.room.application.port.out.RoomParticipantCommandPort;
import konkuk.thip.user.domain.User;
import konkuk.thip.room.domain.RoomParticipant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RoomGetMemberListService implements RoomGetMemberListUseCase {

    private final RoomCommandPort roomCommandPort;
    private final RoomParticipantCommandPort roomParticipantCommandPort;
    private final UserCommandPort userCommandPort;
    private final FollowingQueryPort followingQueryPort;

    @Override
    @Transactional(readOnly = true)
    public RoomGetMemberListResponse getRoomMemberList(Long roomId) {

        // 1. 방 검증 및 방 조회
        Room room = roomCommandPort.findById(roomId);

        // 2. 방 참여자(UserRoom) 전체 조회
        List<RoomParticipant> roomParticipants = roomParticipantCommandPort.findAllByRoomId(room.getId());


        // 3. 참여자 userId 목록 추출
        List<Long> userIds = roomParticipants.stream()
                .map(RoomParticipant::getUserId)
                .toList();

        // 4. 배치 쿼리로 유저 정보, 팔로워 수 조회
        Map<Long, User> userMap = userCommandPort.findByIds(userIds);
        Map<Long, Integer> subscriberCountMap = followingQueryPort.countByFollowingUserIds(userIds);

        // 5. 각 userRoom에 대해 DTO 조립
        List<RoomGetMemberListResponse.MemberSearchResult> userList = roomParticipants.stream()
                .map(userRoom -> {
                    Long userId = userRoom.getUserId();
                    User user = userMap.get(userId);
                    int subscriberCount = subscriberCountMap.getOrDefault(userId, 0);

                    return RoomGetMemberListResponse.MemberSearchResult.builder()
                            .userId(userId)
                            .nickname(user.getNickname())
                            .imageUrl(user.getAlias().getImageUrl())
                            .alias(user.getAlias().getValue())
                            .subscriberCount(subscriberCount)
                            .build();
                })
                .toList();

        // 6. DTO 반환
        return RoomGetMemberListResponse.builder()
                .userList(userList)
                .build();
    }


}
