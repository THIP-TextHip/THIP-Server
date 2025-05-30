package konkuk.thip.user.adapter.in.web;

import konkuk.thip.user.adapter.in.web.response.UserReadResponse;
import konkuk.thip.user.application.port.in.UserQueryFacade;
import konkuk.thip.user.application.port.in.dto.UserReadResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserQueryController {

    private final UserQueryFacade userQueryFacade;

    @GetMapping("/api/test/{userId}")
    public ResponseEntity<UserReadResponse> testRead(@PathVariable Long userId) {
        UserReadResult result = userQueryFacade.readUser(userId);
        UserReadResponse userReadResponse = UserReadResponse.builder()
                .id(result.getId())
                .name(result.getName())
                .email(result.getEmail())
                .build();

        return ResponseEntity.ok(userReadResponse);
    }
}
