package konkuk.thip.record.adapter.in.web;

import jakarta.validation.Valid;
import konkuk.thip.common.dto.BaseResponse;
import konkuk.thip.common.security.annotation.UserId;
import konkuk.thip.record.adapter.in.web.request.RecordCreateRequest;
import konkuk.thip.record.adapter.in.web.response.RecordCreateResponse;
import konkuk.thip.record.application.port.in.RecordCreateUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class RecordCommandController {
    private final RecordCreateUseCase recordCreateUseCase;

    @PostMapping("/rooms/{roomId}/record")
    public BaseResponse<RecordCreateResponse> createRecord(
            @UserId final Long userId,
            @PathVariable final Long roomId,
            @Valid @RequestBody final RecordCreateRequest recordCreateRequest) {
        return BaseResponse.ok(
                RecordCreateResponse.of(
                        recordCreateUseCase.createRecord(recordCreateRequest.toCommand(roomId, userId))
                ));
    }

}
