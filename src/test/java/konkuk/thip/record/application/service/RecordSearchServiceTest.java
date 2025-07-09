package konkuk.thip.record.application.service;

import konkuk.thip.comment.application.port.out.CommentQueryPort;
import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.common.util.DateUtil;
import konkuk.thip.post.application.port.out.PostLikeQueryPort;
import konkuk.thip.record.adapter.in.web.response.RecordSearchResponse;
import konkuk.thip.record.application.port.in.dto.RecordSearchQuery;
import konkuk.thip.record.application.port.in.dto.RecordSearchResult;
import konkuk.thip.record.application.port.out.RecordQueryPort;
import konkuk.thip.record.domain.Record;
import konkuk.thip.user.application.port.out.UserCommandPort;
import konkuk.thip.user.domain.User;
import konkuk.thip.vote.application.port.out.VoteCommandPort;
import konkuk.thip.vote.application.port.out.VoteQueryPort;
import konkuk.thip.vote.domain.Vote;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@DisplayName("[단위] RecordSearchService 테스트")
class RecordSearchServiceTest {

    private RecordQueryPort recordQueryPort;
    private UserCommandPort userCommandPort;
    private PostLikeQueryPort postLikeQueryPort;
    private CommentQueryPort commentQueryPort;
    private VoteCommandPort voteCommandPort;
    private VoteQueryPort voteQueryPort;
    private DateUtil dateUtil;

    private RecordSearchService recordSearchService;

    private final Long userId = 1L;

    @BeforeEach
    void setUp() {
        recordQueryPort = mock(RecordQueryPort.class);
        userCommandPort = mock(UserCommandPort.class);
        postLikeQueryPort = mock(PostLikeQueryPort.class);
        commentQueryPort = mock(CommentQueryPort.class);
        voteCommandPort = mock(VoteCommandPort.class);
        voteQueryPort = mock(VoteQueryPort.class);
        dateUtil = mock(DateUtil.class);

        recordSearchService = new RecordSearchService(
                recordQueryPort,
                userCommandPort,
                postLikeQueryPort,
                commentQueryPort,
                voteCommandPort,
                voteQueryPort,
                dateUtil
        );
    }

    @Test
    @DisplayName("최신순 정렬이 적용된 결과를 반환한다")
    void testSortByLatest() {
        // given
        Record record = Record.builder()
                .id(1L)
                .creatorId(userId)
                .content("레코드")
                .page(1)
                .createdAt(LocalDateTime.now().minusMinutes(10))
                .build();
        Vote vote = Vote.builder()
                .id(2L)
                .creatorId(userId)
                .content("투표")
                .page(1)
                .createdAt(LocalDateTime.now())
                .build();

        when(recordQueryPort.findRecordsByRoom(any(), any(), any(), any(), any(), any()))
                .thenReturn(RecordSearchResult.of(List.of(record), List.of(vote)));

        when(userCommandPort.findById(any())).thenReturn(mock(User.class));
        when(postLikeQueryPort.countByPostId(anyLong())).thenReturn(0);
        when(commentQueryPort.countByPostIdAndUserId(anyLong(), anyLong())).thenReturn(0);
        when(postLikeQueryPort.existsByPostIdAndUserId(anyLong(), anyLong())).thenReturn(false);
        when(voteCommandPort.findVoteItemsByVoteId(anyLong())).thenReturn(emptyList());
        when(voteQueryPort.isUserVoted(anyLong(), anyLong())).thenReturn(false);
        when(dateUtil.formatLastActivityTime(any())).thenReturn("방금 전");

        // when
        RecordSearchResponse response = recordSearchService.search(buildQuery("latest", 1, "mine").build());

        // then
        assertThat(response.recordList()).hasSize(2);
        assertThat(response.recordList().get(0).type()).isEqualTo("RECORD"); // 최신순이므로 Record가 먼저
        assertThat(response.first()).isTrue();
        assertThat(response.last()).isTrue();
        assertThat(response.page()).isEqualTo(1);
    }

    @Test
    @DisplayName("좋아요 순 정렬이 적용된 결과를 반환한다")
    void testSortByLike() {
        // given
        Record record = Record.builder()
                .id(1L)
                .creatorId(userId)
                .content("레코드")
                .page(1)
                .createdAt(LocalDateTime.now())
                .build();
        Vote vote = Vote.builder()
                .id(2L)
                .creatorId(userId)
                .content("투표")
                .page(1)
                .createdAt(LocalDateTime.now())
                .build();

        when(recordQueryPort.findRecordsByRoom(any(), any(), any(), any(), any(), any()))
                .thenReturn(RecordSearchResult.of(List.of(record), List.of(vote)));

        when(userCommandPort.findById(any())).thenReturn(mock(User.class));
        when(postLikeQueryPort.countByPostId(record.getId())).thenReturn(5);
        when(postLikeQueryPort.countByPostId(vote.getId())).thenReturn(10);
        when(commentQueryPort.countByPostIdAndUserId(anyLong(), anyLong())).thenReturn(0);
        when(postLikeQueryPort.existsByPostIdAndUserId(anyLong(), anyLong())).thenReturn(false);
        when(voteCommandPort.findVoteItemsByVoteId(anyLong())).thenReturn(emptyList());
        when(voteQueryPort.isUserVoted(anyLong(), anyLong())).thenReturn(false);
        when(dateUtil.formatLastActivityTime(any())).thenReturn("방금 전");

        // when
        RecordSearchResponse response = recordSearchService.search(buildQuery("like", 1, "mine").build());

        // then
        assertThat(response.recordList().get(0).type()).isEqualTo("VOTE"); // 좋아요가 더 많음
    }

    @Test
    @DisplayName("페이징 처리가 잘 적용되는지 확인")
    void testPagingLogic() {
        // given
        List<Record> records = List.of(
                Record.builder().id(1L).creatorId(userId).content("r1").page(1).createdAt(LocalDateTime.now()).build(),
                Record.builder().id(2L).creatorId(userId).content("r2").page(2).createdAt(LocalDateTime.now()).build(),
                Record.builder().id(3L).creatorId(userId).content("r3").page(3).createdAt(LocalDateTime.now()).build(),
                Record.builder().id(4L).creatorId(userId).content("r4").page(4).createdAt(LocalDateTime.now()).build(),
                Record.builder().id(5L).creatorId(userId).content("r5").page(5).createdAt(LocalDateTime.now()).build(),
                Record.builder().id(6L).creatorId(userId).content("r6").page(6).createdAt(LocalDateTime.now()).build(),
                Record.builder().id(7L).creatorId(userId).content("r7").page(7).createdAt(LocalDateTime.now()).build(),
                Record.builder().id(8L).creatorId(userId).content("r8").page(8).createdAt(LocalDateTime.now()).build(),
                Record.builder().id(9L).creatorId(userId).content("r9").page(9).createdAt(LocalDateTime.now()).build(),
                Record.builder().id(10L).creatorId(userId).content("r10").page(10).createdAt(LocalDateTime.now()).build(),
                Record.builder().id(11L).creatorId(userId).content("r11").page(11).createdAt(LocalDateTime.now()).build()
        );

        List<Vote> votes = List.of(); // 투표 없음

        when(recordQueryPort.findRecordsByRoom(any(), any(), any(), any(), any(), any()))
                .thenReturn(RecordSearchResult.of(records, votes));

        when(userCommandPort.findById(any())).thenReturn(mock(User.class));
        when(postLikeQueryPort.countByPostId(anyLong())).thenReturn(0);
        when(commentQueryPort.countByPostIdAndUserId(anyLong(), anyLong())).thenReturn(0);
        when(postLikeQueryPort.existsByPostIdAndUserId(anyLong(), anyLong())).thenReturn(false);
        when(voteCommandPort.findVoteItemsByVoteId(anyLong())).thenReturn(emptyList());
        when(voteQueryPort.isUserVoted(anyLong(), anyLong())).thenReturn(false);
        when(dateUtil.formatLastActivityTime(any())).thenReturn("방금 전");

        // when
        RecordSearchResponse response = recordSearchService.search(buildQuery("latest", 1, "mine").build());

        // then
        assertThat(response.recordList()).hasSize(10); // 페이지 사이즈만큼 반환
        assertThat(response.size()).isEqualTo(10);
        assertThat(response.page()).isEqualTo(1);
        assertThat(response.first()).isTrue();
        assertThat(response.last()).isFalse(); // 아직 한 개 남음
    }

    private RecordSearchQuery.RecordSearchQueryBuilder buildQuery(String sort, int pageNum, String type) {
        return RecordSearchQuery.builder()
                .roomId(1L)
                .type(type)
                .sort(sort)
                .pageStart(1)
                .pageEnd(10)
                .userId(userId)
                .pageNum(pageNum);
    }

    @Test
    @DisplayName("pageStart만 설정된 경우 예외가 발생한다")
    void testInvalidPageStartOnly() {
        // given
        RecordSearchQuery query = buildQuery("latest", 1, "mine")
                .pageStart(1)
                .pageEnd(null)
                .build();

        // when & then
        assertThrows(InvalidStateException.class, () -> recordSearchService.search(query));
    }

    @Test
    @DisplayName("pageEnd만 설정된 경우 예외가 발생한다")
    void testInvalidPageEndOnly() {
        // given
        RecordSearchQuery query = buildQuery("latest", 1, "mine")
                .pageStart(null)
                .pageEnd(10)
                .build();

        // when & then
        assertThrows(InvalidStateException.class, () -> recordSearchService.search(query));
    }

    @Test
    @DisplayName("pageNum이 0 이하인 경우 예외가 발생한다")
    void testInvalidPageNum() {
        // given
        RecordSearchQuery query = buildQuery("latest", 0, "mine").build();

        // when & then
        assertThrows(InvalidStateException.class, () -> recordSearchService.search(query));
    }

    @Test
    @DisplayName("정의되지 않은 sort 값은 예외를 발생시킨다")
    void testInvalidSort() {
        // given
        RecordSearchQuery query = buildQuery("invalidSort", 1, "mine").build();

        // when & then
        assertThrows(InvalidStateException.class, () -> recordSearchService.search(query));
    }

    @Test
    @DisplayName("정의되지 않은 type 값은 예외를 발생시킨다")
    void testInvalidType() {
        // given
        RecordSearchQuery query = buildQuery("latest", 1, "invalidType")
                .build();

        // when & then
        assertThrows(InvalidStateException.class, () -> recordSearchService.search(query));
    }
}