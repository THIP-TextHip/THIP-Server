package konkuk.thip.room.adapter.in.web.response;

import lombok.Builder;

import java.util.List;

@Builder
public record RoomGetMemberListResponse(

        List<MemberSearchResult> userList
){
        @Builder
        public record MemberSearchResult(
                Long userId,
                String nickname,
                String imageUrl,
                String alias,
                int followerCount
        ) {}
}