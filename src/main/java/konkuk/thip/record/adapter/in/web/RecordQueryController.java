package konkuk.thip.record.adapter.in.web;

import konkuk.thip.common.dto.BaseResponse;
import konkuk.thip.common.security.annotation.UserId;
import konkuk.thip.record.adapter.in.web.response.RecordSearchResponse;
import konkuk.thip.record.application.port.in.dto.RecordSearchUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RecordQueryController {

    private final RecordSearchUseCase recordSearchUseCase;

    @GetMapping("/rooms/{roomId}/posts")
    public BaseResponse<RecordSearchResponse> viewRecordList(
            @PathVariable Long roomId,
            @RequestParam String type,
            @RequestParam String sort,
            @RequestParam(required = false) Integer pageStart,
            @RequestParam(required = false) Integer pageEnd,
            @UserId Long userId
    ) {

        return null;
    }

}
