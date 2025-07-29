package konkuk.thip.user.application.service;

import konkuk.thip.user.adapter.in.web.response.UserSearchResponse;
import konkuk.thip.user.application.mapper.UserQueryMapper;
import konkuk.thip.user.application.port.in.UserSearchUsecase;
import konkuk.thip.user.application.port.in.dto.UserSearchQuery;
import konkuk.thip.user.application.port.out.UserQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserSearchService implements UserSearchUsecase {

    private final UserQueryPort userQueryPort;
    private final UserQueryMapper userQueryMapper;

    @Override
    public UserSearchResponse searchUsers(UserSearchQuery userSearchQuery) {
        var userDtos = userQueryPort.findUsersByNicknameOrderByAccuracy(
                        userSearchQuery.keyword(),
                        userSearchQuery.userId(),
                        userSearchQuery.size()
                ).stream()
                .map(userQueryMapper::toUserDto)
                .toList();
        return UserSearchResponse.of(userDtos);
    }
}
