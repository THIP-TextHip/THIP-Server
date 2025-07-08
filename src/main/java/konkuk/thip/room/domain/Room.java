package konkuk.thip.room.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.common.entity.StatusType;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

import static konkuk.thip.common.exception.code.ErrorCode.INVALID_ROOM_CREATE;

@Getter
@SuperBuilder
public class Room extends BaseDomainEntity {

    private Long id;

    private String title;

    private String description;

    private boolean isPublic;

    private String password;

    private double roomPercentage;

    private LocalDate startDate;

    private LocalDate endDate;

    private int recruitCount;

    private Long bookId;

    private Long categoryId;

    public static Room withoutId(String title, String description, boolean isPublic, String password, LocalDate startDate, LocalDate endDate, int recruitCount, Long bookId, Long categoryId) {
        validateVisibilityPasswordRule(isPublic, password);
        validateDates(startDate, endDate);

        return Room.builder()
                .id(null)
                .title(title)
                .description(description)
                .isPublic(isPublic)
                .password(password)
                .roomPercentage(0)      // 처음 Room 생성 시 -> 0%
                .startDate(startDate)
                .endDate(endDate)
                .recruitCount(recruitCount)
                .bookId(bookId)
                .categoryId(categoryId)
                .build();
    }

    private static void validateVisibilityPasswordRule(boolean isPublic, String password) {
        boolean hasPassword = password != null;

        if ((isPublic && hasPassword) || (!isPublic && !hasPassword)) {
            String message = String.format(
                    "방 공개/비공개 여부와 비밀번호 설정이 일치하지 않습니다. 공개 여부 = %s, 비밀번호 존재 여부 = %s",
                    isPublic, hasPassword
            );
            throw new InvalidStateException(INVALID_ROOM_CREATE,
                    new IllegalArgumentException(message));
        }
    }

    private static void validateDates(LocalDate startDate, LocalDate endDate) {
        LocalDate today = LocalDate.now();
        if (!startDate.isBefore(endDate)) {
            String message = String.format(
                    "시작일(%s)은 종료일(%s)보다 이전이어야 합니다.",
                    startDate, endDate
            );
            throw new InvalidStateException(INVALID_ROOM_CREATE,
                    new IllegalArgumentException(message));
        }

        if (startDate.isBefore(today)) {
            String message = String.format(
                    "시작일(%s)은 현재 날짜(%s) 이후여야 합니다.",     // 현재 날짜 포함
                    startDate, today
            );
            throw new InvalidStateException(INVALID_ROOM_CREATE,
                    new IllegalArgumentException(message));
        }
    }

    public boolean isExpired() {
        return this.getStatus() == StatusType.EXPIRED;
    }

    public void updateRoomPercentage(double roomPercentage) {
        this.roomPercentage = roomPercentage;
    }
}
