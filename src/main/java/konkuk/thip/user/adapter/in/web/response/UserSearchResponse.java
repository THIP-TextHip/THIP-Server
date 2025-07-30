package konkuk.thip.user.adapter.in.web.response;

import java.util.List;

public record UserSearchResponse(
        List<UserDto> userList
) {
    public record UserDto(
            Long userId,
            String nickname,
            String profileImageUrl,
            String aliasName,
            String aliasColor,
            Integer followerCount
    ) {
    }

    public static UserSearchResponse of(List<UserDto> userList) {
        return new UserSearchResponse(userList);
    }
}
