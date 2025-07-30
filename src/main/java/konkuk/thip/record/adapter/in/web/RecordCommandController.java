package konkuk.thip.record.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import konkuk.thip.common.dto.BaseResponse;
import konkuk.thip.common.security.annotation.UserId;
import konkuk.thip.common.swagger.annotation.ExceptionDescription;
import konkuk.thip.record.adapter.in.web.request.RecordCreateRequest;
import konkuk.thip.record.adapter.in.web.response.RecordCreateResponse;
import konkuk.thip.record.application.port.in.RecordCreateUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static konkuk.thip.common.swagger.SwaggerResponseDescription.*;

@Tag(name = "Record Command API", description = "기록 상태변경 관련 API")
@RestController
@RequiredArgsConstructor
public class RecordCommandController {
    private final RecordCreateUseCase recordCreateUseCase;

    @Operation(
            summary = "기록 생성",
            description = "방에 대한 기록을 생성합니다."
    )
    @ExceptionDescription(RECORD_CREATE)
    @PostMapping("/rooms/{roomId}/record")
    public BaseResponse<RecordCreateResponse> createRecord(
            @Parameter(hidden = true) @UserId final Long userId,
            @Parameter(description = "기록을 생성할 방 ID", example = "1") @PathVariable final Long roomId,
            @Valid @RequestBody final RecordCreateRequest recordCreateRequest
    ) {
        return BaseResponse.ok(
                RecordCreateResponse.of(
                        recordCreateUseCase.createRecord(recordCreateRequest.toCommand(roomId, userId))
                ));
    }

}
