package konkuk.thip.user.adapter.in.web;

import konkuk.thip.common.dto.BaseResponse;
import konkuk.thip.user.adapter.in.web.response.UserFollowersResponse;
import konkuk.thip.user.adapter.in.web.response.UserViewAliasChoiceResponse;
import konkuk.thip.user.application.port.in.UserGetFollowersUsecase;
import konkuk.thip.user.application.port.in.UserViewAliasChoiceUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserQueryController {

    private final UserViewAliasChoiceUseCase userViewAliasChoiceUseCase;
    private final UserGetFollowersUsecase userGetFollowersUsecase;

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
                                                             @RequestParam(required = false) final String cursor) {
        return BaseResponse.ok(userGetFollowersUsecase.getUserFollowers(userId, cursor));
    }

    /**
     * 내 팔로잉 리스트 조회
     */
//    @GetMapping("/users/my/following")
//    public BaseResponse<UserFollowersResponse> showMyFollowing(@RequestParam(required = false) final String cursor) {
//        return BaseResponse.ok(userGetFollowersUsecase.getMyFollowing(cursor));
//    }
}
