package konkuk.thip.user.adapter.in.web;

import konkuk.thip.user.adapter.in.web.request.UserSignupRequest;
import konkuk.thip.user.adapter.in.web.request.UserUpdateRequest;
import konkuk.thip.user.application.port.in.UserCommandFacade;
import konkuk.thip.user.application.port.in.dto.UserSignupCommand;
import konkuk.thip.user.application.port.in.dto.UserUpdateCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserCommandController {

    private final UserCommandFacade userCommandFacade;

    @PostMapping("/api/test/signup")
    public ResponseEntity<Long> testSignup(@RequestBody UserSignupRequest request) {
        UserSignupCommand command = UserSignupCommand.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword())
                .build();

        return ResponseEntity.ok(userCommandFacade.signup(command));
    }

    @PutMapping("/api/test/update")
    public void testUpdate(@RequestBody UserUpdateRequest request) {
        UserUpdateCommand command = UserUpdateCommand.builder()
                .id(request.getId())
                .name(request.getName())
                .password(request.getPassword())
                .build();

        userCommandFacade.updateUser(command);
    }
}
