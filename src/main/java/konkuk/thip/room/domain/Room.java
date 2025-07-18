package konkuk.thip.room.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.common.entity.StatusType;
import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.common.exception.code.ErrorCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

import static konkuk.thip.common.exception.code.ErrorCode.*;

@Getter
@SuperBuilder
public class Room extends BaseDomainEntity {

    private static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    private Long id;

    private String title;

    private String description;

    private boolean isPublic;

    private String hashedPassword;

    private double roomPercentage;

    private LocalDate startDate;

    private LocalDate endDate;

    private int recruitCount;

    private int memberCount;

    private Long bookId;

    private Category category;

    public static Room withoutId(String title, String description, boolean isPublic, String password, LocalDate startDate, LocalDate endDate, int recruitCount, Long bookId, Category category) {
        validateVisibilityPasswordRule(isPublic, password);
        validateDates(startDate, endDate);

        // 비밀번호 해싱
        String hashedPassword = (password != null) ? PASSWORD_ENCODER.encode(password) : null;

        return Room.builder()
                .id(null)
                .title(title)
                .description(description)
                .isPublic(isPublic)
                .hashedPassword(hashedPassword)
                .roomPercentage(0)      // 처음 Room 생성 시 -> 0%
                .startDate(startDate)
                .endDate(endDate)
                .recruitCount(recruitCount)
                .memberCount(1) // 처음 Room 생성 시 방장 1명
                .bookId(bookId)
                .category(category)
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

        if (!startDate.isAfter(today)) {
            String message = String.format(
                    "시작일(%s)은 현재 날짜(%s) 이후여야 합니다.",     // 현재 날짜 미포함
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

    public boolean matchesPassword(String rawPassword) {
        if (this.hashedPassword == null || rawPassword == null) {
            return false;
        }
        return PASSWORD_ENCODER.matches(rawPassword, this.hashedPassword);
    }

    public void verifyPassword(String rawPassword) {

        validateRoomExpired();

        // 공개방일 경우 비밀번호 입력 요청 예외 처리
        if (this.isPublic()) {
            throw new BusinessException(ROOM_PASSWORD_NOT_REQUIRED);
        }

        //비밀번호 검증
        if (!matchesPassword(rawPassword)) {
            throw new BusinessException(ROOM_PASSWORD_MISMATCH);
        }
    }

    public void validateRoomExpired() {
        // 모집기간 만료 체크
        LocalDate deadline = this.startDate.minusDays(1);
        if (isRecruitmentPeriodExpired()) {
            String message = String.format("모집기간(%s까지)이 만료된 방에는 참여할 수 없습니다.", deadline);
            throw new BusinessException(
                    ErrorCode.ROOM_RECRUITMENT_PERIOD_EXPIRED, new IllegalArgumentException(message)
            );
        }
    }

    public boolean isRecruitmentPeriodExpired() {
        LocalDate today = LocalDate.now();
        // 모집 마감일: startDate.minusDays(1)
        return today.isAfter(this.startDate.minusDays(1));
    }

    public void increaseMemberCount() {
        checkJoinPossible();
        memberCount++;
    }

    public void decreaseMemberCount() {
        checkCancelPossible();
        memberCount--;
    }

    private void checkJoinPossible() {
        if (memberCount >= recruitCount) {
            throw new InvalidStateException(ErrorCode.ROOM_MEMBER_COUNT_EXCEEDED);
        }
    }

    private void checkCancelPossible() {
        if (memberCount <= 1) { // 방장 포함 항상 1명 이상이어야 함
            throw new InvalidStateException(ErrorCode.ROOM_MEMBER_COUNT_UNDERFLOW);
        }
    }

    public void startRoom() {
        validateRoomExpired();

        if (startDate.isBefore(LocalDate.now()) || startDate.isEqual(LocalDate.now())) {
            throw new InvalidStateException(ErrorCode.ROOM_ALREADY_STARTED);
        }
        startDate = LocalDate.now();
    }


}
