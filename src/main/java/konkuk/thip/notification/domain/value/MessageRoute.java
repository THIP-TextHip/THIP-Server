package konkuk.thip.notification.domain.value;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MessageRoute {
    // NONE
    NONE,                   // 알림 클릭 시 이동하지 않음

    // FEED
    FEED_USER,              // 자신을 팔로우한 사용자의 피드 목록으로 화면 이동
    FEED_DETAIL,            // 특정 피드 상세 화면으로 이동

    // ROOM
    ROOM_MAIN,              // 특정 모임방 메인 화면으로 이동
    ROOM_DETAIL,            // 특정 모임 상세정보 화면으로 이동
    ROOM_POST_DETAIL,       // 특정 모임 게시글 상세 화면으로 이동 -> PostType으로 투표(VOTE)인지 기록(RECORD)인지 판단
}
