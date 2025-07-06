package konkuk.thip.user.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("[단위] UserRoom 도메인 테스트")
class UserRoomTest {

    @Test
    @DisplayName("canWriteOverview: 사용자 퍼센트가 80 이상일 때, true를 반환한다.")
    void can_writeOverview() {
        //given
        UserRoom userRoom = UserRoom.builder()
                .userPercentage(80.0)
                .build();

        //when
        boolean canWrite = userRoom.canWriteOverview();

        //then
        assertThat(canWrite).isTrue();
    }

    @Test
    @DisplayName("canWriteOverview: 사용자 퍼센트가 80 미만일 때, false를 반환한다.")
    void cannot_writeOverview() {
        //given
        UserRoom userRoom = UserRoom.builder()
                .userPercentage(79.9)
                .build();

        //when
        boolean canWrite = userRoom.canWriteOverview();

        //then
        assertThat(canWrite).isFalse();
    }

    @Test
    @DisplayName("updateUserProgress: 요청 페이지가 현재 페이지보다 클 때, 현재 페이지와 사용자 퍼센트가 업데이트된다.")
    void update_userProgress_success() throws Exception {
        //given
        UserRoom userRoom = UserRoom.builder()
                .currentPage(1)
                .userPercentage(5.0)
                .userId(1L)
                .roomId(1L)
                .build();

        //when
        int totalPageCount = 20;
        boolean isUpdated = userRoom.updateUserProgress(5, totalPageCount);

        //then
        assertThat(isUpdated).isTrue();
        assertThat(userRoom.getCurrentPage()).isEqualTo(5);
        double ratio = (double) userRoom.getCurrentPage() / totalPageCount;
        assertThat(userRoom.getUserPercentage()).isEqualTo(ratio * 100);
    }

    @Test
    @DisplayName("updateUserProgress: 요청 페이지가 현재 페이지보다 작을 때, 현재 페이지와 사용자 퍼센트가 업데이트되지 않는다.")
    void update_userProgress_when_request_isLower_than_bookPage() throws Exception {
        //given
        UserRoom userRoom = UserRoom.builder()
                .currentPage(5)
                .userPercentage(25.0)
                .userId(1L)
                .roomId(1L)
                .build();

        //when
        int totalPageCount = 20;
        boolean isUpdated = userRoom.updateUserProgress(3, totalPageCount);

        //then
        assertThat(isUpdated).isFalse();
        assertThat(userRoom.getCurrentPage()).isEqualTo(5);
        double ratio = (double) userRoom.getCurrentPage() / totalPageCount;
        assertThat(userRoom.getUserPercentage()).isEqualTo(ratio * 100);
    }
}