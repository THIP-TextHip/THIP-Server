package konkuk.thip.room.adapter.in.web.response;

import lombok.Builder;

import java.util.List;

@Builder
public record RoomGetMemberListResponse(

        List<MemberSearchResult> userList
){
        @Builder
        public record MemberSearchResult(
                Long userId,        // 이거 반환안해도 될텐데??
                String nickname,
                String imageUrl,
                String aliasName,
                String aliasColor,
                int followerCount,
                boolean isMyself
        ) {}
}