package konkuk.thip.room.adapter.in.web;

import konkuk.thip.common.dto.BaseResponse;
import konkuk.thip.room.adapter.in.web.response.RoomSearchResponse;
import konkuk.thip.room.application.port.in.RoomSearchUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RoomQueryController {

    private final RoomSearchUseCase roomSearchUseCase;

    @GetMapping("/rooms/search")
    public BaseResponse<RoomSearchResponse> searchRooms(
            @RequestParam(value = "keyword", required = false, defaultValue = "") final String keyword,
            @RequestParam(value = "category", required = false, defaultValue = "") final String category,
            @RequestParam("sort") final String sort,
            @RequestParam("page") final int page
    ) {
        return BaseResponse.ok(roomSearchUseCase.searchRoom(keyword, category, sort, page));
    }
}
