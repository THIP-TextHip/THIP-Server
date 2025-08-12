package konkuk.thip.room.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.*;
import konkuk.thip.room.application.port.in.dto.RoomCreateCommand;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Schema(description = "방 생성 요청 DTO")
public record RoomCreateRequest(
        @Schema(description = "모임방에서 기록 공유할 책의 ISBN", example = "9788936433862")
        @NotBlank(message = "ISBN은 필수입니다.")
        String isbn,

        @Schema(description = "모임방 카테고리", example = "문학")
        @NotBlank(message = "카테고리는 필수입니다.")
        String category,

        @Schema(description = "모임방 이름", example = "문학 모임")
        @NotBlank(message = "방 이름은 필수입니다.")
        String roomName,

        @Schema(description = "모임방 설명", example = "문학을 사랑하는 사람들의 모임입니다.")
        @NotBlank(message = "설명은 필수입니다.")
        String description,

        @Schema(description = "진행 시작일", example = "2023.10.01")
        @Pattern(
                regexp = "\\d{4}\\.\\d{2}\\.\\d{2}",
                message = "진행 시작일은 yyyy.MM.dd 형식이어야 합니다."
        )
        String progressStartDate,

        @Schema(description = "진행 종료일", example = "2023.10.31")
        @Pattern(
                regexp = "\\d{4}\\.\\d{2}\\.\\d{2}",
                message = "진행 종료일은 yyyy.MM.dd 형식이어야 합니다."
        )
        String progressEndDate,

        @Schema(description = "모집 인원 (1~30)", example = "5")
        @Min(value = 1, message = "모집 인원은 최소 1명이어야 합니다.")
        @Max(value = 30, message = "모집 인원은 최대 30명이어야 합니다.")
        int recruitCount,

        @Schema(description = "방 비밀번호 (숫자 4자리)", example = "1234")
        @Nullable
        @Pattern(regexp = "\\d{4}", message = "비밀번호는 숫자 4자리여야 합니다.")
        String password,

        @Schema(description = "방 공개 설정 여부 (true: 공개, false: 비공개)", example = "true")
        @NotNull(message = "방 공개 설정 여부는 필수입니다.")
        Boolean isPublic
) {
        public RoomCreateCommand toCommand() {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
                return new RoomCreateCommand(
                        isbn,
                        category,
                        roomName,
                        description,
                        LocalDate.parse(progressStartDate, formatter),
                        LocalDate.parse(progressEndDate, formatter),
                        recruitCount,
                        password,
                        isPublic
                );
        }
}
