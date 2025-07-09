package konkuk.thip.room.adapter.out.persistence;

import konkuk.thip.room.adapter.in.web.response.RoomSearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RoomQueryRepository {

    Page<RoomSearchResponse.RoomSearchResult> searchRoom(String keyword, String category, Pageable pageable);
}
