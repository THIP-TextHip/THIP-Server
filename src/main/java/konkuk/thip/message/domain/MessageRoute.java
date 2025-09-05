package konkuk.thip.message.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MessageRoute {
    // FEED
    FEED_USER("FEED_USER"),         // 자신을 팔로우한 사용자의 피드 목록으로 화면 이동
    FEED_DETAIL("FEED_DETAIL"),     // 특정 피드 상세 화면으로 이동

    // ROOM
    ROOM_MAIN("ROOM_MAIN"),        // 특정 모임방 메인 화면으로 이동
    ROOM_DETAIL("ROOM_DETAIL"),   // 특정 모임 상세정보 화면으로 이동
    ROOM_POST_DETAIL("ROOM_POST_DETAIL"), // 특정 모임 게시글 상세 화면으로 이동 -> PostType으로 투표인지 기록인지 판단
    ROOM_RECORD_DETAIL("ROOM_RECORD_DETAIL"), // 특정 모임 기록 상세 화면으로 이동 (기록장 조회 - 페이지 필터 걸린채로)
    ROOM_VOTE_DETAIL("ROOM_VOTE_DETAIL");   // 특정 모임 투표 상세 화면으로 이동 (투표 조회 - 페이지 필터 걸린채로)

    private final String code;
}