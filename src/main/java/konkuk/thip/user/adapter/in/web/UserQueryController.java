package konkuk.thip.user.adapter.in.web;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import konkuk.thip.common.dto.BaseResponse;
import konkuk.thip.common.security.annotation.UserId;
import konkuk.thip.user.adapter.in.web.response.*;
import konkuk.thip.user.application.port.in.UserGetFollowUsecase;
import konkuk.thip.user.application.port.in.UserIsFollowingUsecase;
import konkuk.thip.user.application.port.in.UserSearchUsecase;
import konkuk.thip.user.application.port.in.UserViewAliasChoiceUseCase;
import konkuk.thip.user.application.port.in.dto.UserSearchQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
public class UserQueryController {

    private final UserViewAliasChoiceUseCase userViewAliasChoiceUseCase;
    private final UserGetFollowUsecase userGetFollowUsecase;
    private final UserIsFollowingUsecase userIsFollowingUsecase;
    private final UserSearchUsecase userSearchUsecase;

    /**
     * 사용자 별칭 선택 화면 조회
     */
    @GetMapping("/users/alias")
    public BaseResponse<UserViewAliasChoiceResponse> showAliasChoiceView() {
        return BaseResponse.ok(UserViewAliasChoiceResponse.of(
                userViewAliasChoiceUseCase.getAllAliasesAndCategories()
        ));
    }

    /**
     * 사용자 팔로워 조회
     */
    @GetMapping("/users/{userId}/followers")
    public BaseResponse<UserFollowersResponse> showFollowers(@PathVariable final Long userId,
                                                             @RequestParam(required = false) final String cursor,
                                                             @RequestParam(defaultValue = "10") @Max(value = 10) @Min(value = 1) final int size) {
        return BaseResponse.ok(userGetFollowUsecase.getUserFollowers(userId, cursor, size));
    }

    /**
     * 내 팔로잉 리스트 조회
     */
    @GetMapping("/users/my/following")
    public BaseResponse<UserFollowingResponse> showMyFollowing(@UserId final Long userId,
                                                               @RequestParam(required = false) final String cursor,
                                                               @RequestParam(defaultValue = "10") @Max(value = 10) @Min(value = 1) final int size) {
        return BaseResponse.ok(userGetFollowUsecase.getMyFollowing(userId, cursor, size));
    }

    /**
     * 팔로잉 여부 조회
     */
    @GetMapping("/users/{targetUserId}/is-following")
    public BaseResponse<UserIsFollowingResponse> checkisFollowing(@UserId final Long userId,
                                                                  @PathVariable final Long targetUserId) {
        return BaseResponse.ok(UserIsFollowingResponse.of(userIsFollowingUsecase.isFollowing(userId, targetUserId)));
    }


    @GetMapping("/users")
    public BaseResponse<UserSearchResponse> showSearchUsers(@RequestParam @NotBlank(message = "검색어는 필수입니다.") final String keyword,
                                                            @RequestParam(required = false, defaultValue = "30") @Min(1) @Max(30) final Integer size,
                                                            @UserId final Long userId) {
        return BaseResponse.ok(userSearchUsecase.searchUsers(UserSearchQuery.of(keyword, userId, size)));
    }
}
