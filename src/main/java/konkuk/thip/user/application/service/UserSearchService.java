package konkuk.thip.user.application.service;

import konkuk.thip.recentSearch.adapter.out.jpa.RecentSearchType;
import konkuk.thip.recentSearch.application.service.manager.RecentSearchCreateManager;
import konkuk.thip.user.adapter.in.web.response.UserSearchResponse;
import konkuk.thip.user.application.mapper.UserQueryMapper;
import konkuk.thip.user.application.port.in.UserSearchUsecase;
import konkuk.thip.user.application.port.in.dto.UserSearchQuery;
import konkuk.thip.user.application.port.out.UserQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserSearchService implements UserSearchUsecase {

    private final UserQueryPort userQueryPort;
    private final UserQueryMapper userQueryMapper;

    private final RecentSearchCreateManager recentSearchCreateManager;

    @Override
    @Transactional // <- 최근 검색 저장으로 인한 트랜잭션
    public UserSearchResponse searchUsers(UserSearchQuery userSearchQuery) {
        var userDtoList = userQueryMapper.toUserDtoList(userQueryPort.findUsersByNicknameOrderByAccuracy(
                        userSearchQuery.keyword().toLowerCase(),
                        userSearchQuery.userId(),
                        userSearchQuery.size()
                ));

        recentSearchCreateManager.saveRecentSearchByUser(userSearchQuery.userId(), userSearchQuery.keyword(), RecentSearchType.USER_SEARCH, userSearchQuery.isFinalized());
        
        return UserSearchResponse.of(userDtoList);
    }
}
