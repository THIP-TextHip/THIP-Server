package konkuk.thip.user.application.service;

import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.user.application.port.in.dto.UserUpdateCommand;
import konkuk.thip.user.application.port.out.UserCommandPort;
import konkuk.thip.user.application.port.out.UserQueryPort;
import konkuk.thip.user.domain.Alias;
import konkuk.thip.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@DisplayName("[단위] UserUpdateService 단위 테스트")
class UserUpdateServiceTest {

    private UserCommandPort userCommandPort;
    private UserQueryPort userQueryPort;
    private UserUpdateService userUpdateService;
    private User existingUser;

    @BeforeEach
    void setUp() {
        userCommandPort = mock(UserCommandPort.class);
        userQueryPort = mock(UserQueryPort.class);
        userUpdateService = new UserUpdateService(userCommandPort, userQueryPort);

        existingUser = User.builder()
                .id(1L)
                .nickname("thip")
                .nicknameUpdatedAt(LocalDate.now().minusMonths(7))
                .userRole("USER")
                .oauth2Id("oauth2-id")
                .followerCount(0)
                .alias(Alias.WRITER)
                .build();
    }

    @Test
    @DisplayName("닉네임과 별칭을 정상적으로 업데이트")
    void updateUser_success() {
        // given
        UserUpdateCommand command = new UserUpdateCommand(Alias.ARTIST.getValue(), "newthip", existingUser.getId());
        when(userCommandPort.findById(command.userId())).thenReturn(existingUser);
        when(userQueryPort.existsByNicknameAndUserIdNot(command.nickname(), command.userId()))
                .thenReturn(false);

        // when
        userUpdateService.updateUser(command);

        // then
        assertThat(existingUser.getNickname()).isEqualTo("newthip");
        assertThat(existingUser.getAlias()).isEqualTo(Alias.ARTIST);
        verify(userCommandPort).update(existingUser);
    }

    @Test
    @DisplayName("닉네임 중복이면 예외 발생")
    void updateUser_duplicateNickname_throwsException() {
        // given
        UserUpdateCommand command = new UserUpdateCommand(Alias.ARTIST.getValue(), "existingnickname", existingUser.getId());
        when(userCommandPort.findById(command.userId())).thenReturn(existingUser);
        when(userQueryPort.existsByNicknameAndUserIdNot(command.nickname(), command.userId()))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() -> userUpdateService.updateUser(command))
                .isInstanceOf(BusinessException.class);
        verify(userCommandPort, never()).update(any());
    }

    @Test
    @DisplayName("닉네임 없이 별칭만 업데이트")
    void updateUser_onlyAlias_success() {
        // given
        UserUpdateCommand command = new UserUpdateCommand(Alias.SCIENTIST.getValue(), null, existingUser.getId());
        when(userCommandPort.findById(command.userId())).thenReturn(existingUser);

        // when
        userUpdateService.updateUser(command);

        // then
        assertThat(existingUser.getAlias()).isEqualTo(Alias.SCIENTIST);
        assertThat(existingUser.getNickname()).isEqualTo("thip");
        verify(userCommandPort).update(existingUser);
    }
}