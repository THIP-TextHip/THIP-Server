package konkuk.thip.record.application.service;

import konkuk.thip.book.application.port.out.BookCommandPort;
import konkuk.thip.book.domain.Book;
import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.post.application.port.out.PostLikeQueryPort;
import konkuk.thip.record.adapter.in.web.response.RecordDto;
import konkuk.thip.record.adapter.in.web.response.RecordSearchResponse;
import konkuk.thip.record.application.port.out.RecordQueryPort;
import konkuk.thip.vote.application.port.out.VoteCommandPort;
import konkuk.thip.vote.application.port.out.VoteQueryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@DisplayName("[단위] RecordSearchService 테스트")
class RecordSearchServiceTest {

    private RecordQueryPort recordQueryPort;
    private BookCommandPort bookCommandPort;
    private VoteCommandPort voteCommandPort;
    private VoteQueryPort voteQueryPort;
    private PostLikeQueryPort postLikeQueryPort;

    private RecordSearchService recordSearchService;

    @BeforeEach
    void setUp() {
        recordQueryPort = mock(RecordQueryPort.class);
        bookCommandPort = mock(BookCommandPort.class);
        voteCommandPort = mock(VoteCommandPort.class);
        voteQueryPort = mock(VoteQueryPort.class);
        postLikeQueryPort = mock(PostLikeQueryPort.class);

        recordSearchService = new RecordSearchService(
                recordQueryPort, bookCommandPort, voteCommandPort, voteQueryPort, postLikeQueryPort);
    }

    @Test
    @DisplayName("전체 페이지 조회 성공")
    void search_all_pages_success() {
        Long roomId = 1L;
        String type = "group";
        String sort = "latest";
        Integer pageStart = null;
        Integer pageEnd = null;
        Boolean isOverview = false;
        Integer pageNum = 1;
        Long userId = 1L;

        when(bookCommandPort.findBookByRoomId(roomId)).thenReturn(Book.builder().pageCount(100).build());
        RecordDto mockDto = new RecordDto("방금 전", 1, 1L, "사용자", "url", "내용", 1, 1, false, true, 1L);
        when(recordQueryPort.findRecordsByRoom(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(mockDto)));
        when(postLikeQueryPort.existsByPostIdAndUserId(1L, userId)).thenReturn(true);

        RecordSearchResponse response = recordSearchService.search(roomId, type, sort, pageStart, pageEnd, isOverview, pageNum, userId);

        assertThat(response.recordList()).hasSize(1);
        assertThat(((RecordDto) response.recordList().get(0)).isLiked()).isTrue();
    }

    @Test
    @DisplayName("pageStart와 pageEnd가 하나만 null이면 예외 발생")
    void invalid_page_range() {
        Long roomId = 1L;
        String type = null;
        String sort = "latest";
        Integer pageStart = 1;
        Integer pageEnd = null;
        Boolean isOverview = false;
        Integer pageNum = 1;
        Long userId = 1L;

        assertThrows(InvalidStateException.class, () ->
                recordSearchService.search(roomId, type, sort, pageStart, pageEnd, isOverview, pageNum, userId));
    }

    @Test
    @DisplayName("pageStart > pageEnd면 예외 발생")
    void invalid_page_range_order() {
        Long roomId = 1L;
        Integer pageStart = 10;
        Integer pageEnd = 5;
        Boolean isOverview = false;
        Integer pageNum = 1;
        Long userId = 1L;

        assertThrows(InvalidStateException.class, () ->
                recordSearchService.search(roomId, null, null, pageStart, pageEnd, isOverview, pageNum, userId));
    }

    @Test
    @DisplayName("pageNum이 1보다 작으면 예외 발생")
    void invalid_pageNum() {
        Long roomId = 1L;
        Integer pageNum = 0;

        assertThrows(InvalidStateException.class, () ->
                recordSearchService.search(roomId, null, null, null, null, true, pageNum, 1L));
    }

    @Test
    @DisplayName("pageNum이 null인 경우 첫번째 페이지 조회")
    void search_with_null_pageNum() {
        Long roomId = 1L;
        String type = "group";
        String sort = "latest";
        Integer pageStart = null;
        Integer pageEnd = null;
        Boolean isOverview = true;
        Long userId = 1L;

        RecordDto mockDto = new RecordDto("방금 전", 1, 1L, "사용자", "url", "내용", 1, 1, false, true, 1L);
        when(recordQueryPort.findRecordsByRoom(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(mockDto)));
        when(postLikeQueryPort.existsByPostIdAndUserId(1L, userId)).thenReturn(true);

        RecordSearchResponse response = recordSearchService.search(roomId, type, sort, pageStart, pageEnd, isOverview, null, userId);

        assertThat(response.page()).isEqualTo(1);
        assertThat(response.recordList()).hasSize(1);
    }

    @Test
    @DisplayName("형식이 맞지 않는 type으로 조회 시 예외 발생")
    void search_with_invalid_type() {
        Long roomId = 1L;
        String type = "invalidType";
        String sort = "latest";
        Integer pageStart = 1;
        Integer pageEnd = 10;
        Boolean isOverview = true;
        Integer pageNum = 1;
        Long userId = 1L;

        assertThrows(InvalidStateException.class, () ->
                recordSearchService.search(roomId, type, sort, pageStart, pageEnd, isOverview, pageNum, userId));
    }

    @Test
    @DisplayName("형식이 맞지 않는 sort로 조회 시 예외 발생")
    void search_with_invalid_sort() {
        Long roomId = 1L;
        String type = "group";
        String sort = "invalidSort";
        Integer pageStart = 1;
        Integer pageEnd = 10;
        Boolean isOverview = true;
        Integer pageNum = 1;
        Long userId = 1L;

        assertThrows(InvalidStateException.class, () ->
                recordSearchService.search(roomId, type, sort, pageStart, pageEnd, isOverview, pageNum, userId));
    }
}