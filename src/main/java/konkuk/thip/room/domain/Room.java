package konkuk.thip.room.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import konkuk.thip.common.exception.InvalidStateException;
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

    public static Room withoutId(String title, String description, boolean isPublic, String password, double roomPercentage, LocalDate startDate, LocalDate endDate, int recruitCount, Long bookId, Long categoryId) {
        validateVisibilityPasswordRule(isPublic, password);

        return Room.builder()
                .id(null)
                .title(title)
                .description(description)
                .isPublic(isPublic)
                .password(password)
                .roomPercentage(roomPercentage)
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
}
