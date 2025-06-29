package konkuk.thip.user.adapter.in.web;

import konkuk.thip.common.dto.BaseResponse;
import konkuk.thip.user.adapter.in.web.response.GetUserShowAliasChoiceResponse;
import konkuk.thip.user.application.port.in.ShowAliasChoiceViewUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserQueryController {

    private final ShowAliasChoiceViewUseCase showAliasChoiceViewUseCase;

    @GetMapping("/users/alias")
    public BaseResponse<GetUserShowAliasChoiceResponse> showAliasChoiceView() {
        return BaseResponse.ok(GetUserShowAliasChoiceResponse.of(
                showAliasChoiceViewUseCase.getAllAliasesAndCategories()
        ));
    }
}
