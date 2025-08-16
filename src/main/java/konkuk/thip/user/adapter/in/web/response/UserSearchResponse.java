package konkuk.thip.user.adapter.in.web.response;

import java.util.List;

public record UserSearchResponse(
        List<UserSearchDto> userList
) {
    public record UserSearchDto(
            Long userId,
            String nickname,
            String profileImageUrl,
            String aliasName,
            String aliasColor,
            Integer followerCount
    ) {
    }

    public static UserSearchResponse of(List<UserSearchDto> userList) {
        return new UserSearchResponse(userList);
    }
}
