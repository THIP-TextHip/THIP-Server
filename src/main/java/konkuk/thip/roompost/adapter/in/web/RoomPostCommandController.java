package konkuk.thip.roompost.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import konkuk.thip.common.dto.BaseResponse;
import konkuk.thip.common.security.annotation.UserId;
import konkuk.thip.common.swagger.annotation.ExceptionDescription;
import konkuk.thip.roompost.adapter.in.web.request.*;
import konkuk.thip.roompost.adapter.in.web.response.*;
import konkuk.thip.roompost.application.port.in.*;
import konkuk.thip.roompost.application.port.in.dto.record.RecordDeleteCommand;
import konkuk.thip.roompost.application.port.in.dto.vote.VoteDeleteCommand;
import konkuk.thip.roompost.application.port.in.dto.vote.VoteResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static konkuk.thip.common.swagger.SwaggerResponseDescription.*;

@Tag(name = "RoomPost Command API", description = "방 게시글 상태변경 관련 API")
@RestController
@RequiredArgsConstructor
public class RoomPostCommandController {
    private final RecordCreateUseCase recordCreateUseCase;
    private final RecordDeleteUseCase recordDeleteUseCase;
    private final RoomPostUpdateUseCase roomPostUpdateUseCase;

    private final VoteCreateUseCase voteCreateUseCase;
    private final VoteDeleteUseCase voteDeleteUseCase;
    private final VoteUseCase voteUseCase;

    private final AttendanceCheckCreateUseCase attendanceCheckCreateUseCase;
    private final AttendanceCheckDeleteUseCase attendanceCheckDeleteUseCase;
    private final AttendanceCheckReportUseCase attendanceCheckReportUseCase;

    private final RecordReviewCreateUseCase recordReviewCreateUseCase;

    private final RecordReportUseCase recordReportUseCase;
    private final VoteReportUseCase voteReportUseCase;

    /**
     * 기록 관련
     */
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

    @Operation(
            summary = "기록 삭제",
            description = "사용자가 기록을 삭제합니다."
    )
    @ExceptionDescription(RECORD_DELETE)
    @DeleteMapping("/rooms/{roomId}/record/{recordId}")
    public BaseResponse<RecordDeleteResponse> deleteRecord(
            @Parameter(description = "삭제하려는 기록 ID", example = "1") @PathVariable("recordId") final Long recordId,
            @Parameter(description = "삭제하려는 기록이 작성된 모임 ID", example = "1") @PathVariable("roomId") final Long roomId,
            @Parameter(hidden = true) @UserId final Long userId) {
        return BaseResponse.ok(RecordDeleteResponse.of(recordDeleteUseCase.deleteRecord(new RecordDeleteCommand(roomId, recordId, userId))));
    }

    @Operation(
            summary = "기록 수정",
            description = "사용자가 방 기록을 수정합니다. (기록 내용만 수정 가능)"
    )
    @PatchMapping("/rooms/{roomId}/records/{recordId}")
    @ExceptionDescription(RECORD_UPDATE)
    public BaseResponse<RecordUpdateResponse> updateRecord(
            @Parameter(hidden = true) @UserId Long userId,
            @Parameter(description = "수정할 방 ID", example = "1") @PathVariable Long roomId,
            @Parameter(description = "수정할 기록 ID", example = "1") @PathVariable Long recordId,
            @RequestBody @Valid final RecordUpdateRequest request
    ) {
        return BaseResponse.ok(RecordUpdateResponse.of(
                roomPostUpdateUseCase.updateRecord(request.toCommand(userId, roomId, recordId))
        ));
    }

    @Operation(
            summary = "기록 신고",
            description = "사용자가 기록을 신고합니다. 신고 횟수만 증가하며 별도의 처리는 이루어지지 않습니다."
    )
    @ExceptionDescription(RECORD_REPORT)
    @PostMapping("/rooms/{roomId}/record/{recordId}/report")
    public BaseResponse<RecordReportResponse> reportRecord(
            @Parameter(description = "신고할 방 ID", example = "1") @PathVariable("roomId") final Long roomId,
            @Parameter(description = "신고하려는 기록 ID", example = "1") @PathVariable("recordId") final Long recordId) {
        return BaseResponse.ok(RecordReportResponse.of(recordReportUseCase.reportRecord(recordId)));
    }

    /**
     * 투표 관련
     */

    @Operation(
            summary = "투표 생성",
            description = "방에 대한 투표를 생성합니다."
    )
    @PostMapping("/rooms/{roomId}/vote")
    public BaseResponse<VoteCreateResponse> createVote(
            @Parameter(hidden = true) @UserId Long userId,
            @Parameter(description = "투표를 생성할 방 ID", example = "1") @PathVariable Long roomId,
            @Valid @RequestBody VoteCreateRequest request) {

        return BaseResponse.ok(VoteCreateResponse.of(
                voteCreateUseCase.createVote(request.toCommand(userId, roomId))
        ));
    }

    @Operation(
            summary = "투표하기",
            description = "특정 투표에 대해 사용자가 투표를 진행합니다. type이 true이면 투표하기, false이면 투표 취소입니다."
    )
    @ExceptionDescription(VOTE)
    @PostMapping("/rooms/{roomId}/vote/{voteId}")
    public BaseResponse<VoteResponse> vote(
            @Parameter(hidden = true) @UserId Long userId,
            @Parameter(description = "투표를 진행할 방 ID", example = "1") @PathVariable Long roomId,
            @Parameter(description = "투표할 투표 ID", example = "1") @PathVariable Long voteId,
            @Valid @RequestBody VoteRequest request) {
        VoteResult voteResult = voteUseCase.vote(request.toCommand(userId, roomId, voteId));
        return BaseResponse.ok(VoteResponse.of(voteResult.postId(), voteResult.roomId(), voteResult.voteItems()));
    }

    @Operation(
            summary = "투표 삭제",
            description = "사용자가 투표를 삭제합니다."
    )
    @ExceptionDescription(VOTE_DELETE)
    @DeleteMapping("/rooms/{roomId}/vote/{voteId}")
    public BaseResponse<VoteDeleteResponse> deleteVote(
            @Parameter(description = "삭제하려는 투표 ID", example = "1") @PathVariable("voteId") final Long voteId,
            @Parameter(description = "삭제하려는 투표가 작성된 모임 ID", example = "1") @PathVariable("roomId") final Long roomId,
            @Parameter(hidden = true) @UserId final Long userId) {
        return BaseResponse.ok(VoteDeleteResponse.of(voteDeleteUseCase.deleteVote(new VoteDeleteCommand(roomId, voteId, userId))));
    }

    @Operation(
            summary = "투표 수정",
            description = "사용자가 방 투표를 수정합니다. (투표 내용만 수정 가능)"
    )
    @PatchMapping("/rooms/{roomId}/votes/{voteId}")
    @ExceptionDescription(VOTE_UPDATE)
    public BaseResponse<VoteUpdateResponse> updateVote(
            @Parameter(hidden = true) @UserId Long userId,
            @Parameter(description = "수정할 방 ID", example = "1") @PathVariable Long roomId,
            @Parameter(description = "수정할 투표 ID", example = "1") @PathVariable Long voteId,
            @RequestBody @Valid final VoteUpdateRequest request
    ) {
        return BaseResponse.ok(VoteUpdateResponse.of(
                roomPostUpdateUseCase.updateVote(request.toCommand(userId, roomId, voteId))
        ));
    }

    @Operation(
            summary = "투표 신고",
            description = "사용자가 투표를 신고합니다. 신고 횟수만 증가하며 별도의 처리는 이루어지지 않습니다."
    )
    @ExceptionDescription(VOTE_REPORT)
    @PostMapping("/rooms/{roomId}/vote/{voteId}/report")
    public BaseResponse<VoteReportResponse> reportVote(
            @Parameter(description = "신고할 방 ID", example = "1") @PathVariable("roomId") final Long roomId,
            @Parameter(description = "신고하려는 투표 ID", example = "1") @PathVariable("voteId") final Long voteId) {
        return BaseResponse.ok(VoteReportResponse.of(voteReportUseCase.reportVote(voteId)));
    }

    /**
     * 오늘의 한마디 관련
     */
    @Operation(
            summary = "오늘의 한마디 작성",
            description = "방 참여자가 오늘의 한마디를 작성합니다."
    )
    @ExceptionDescription(ATTENDANCE_CHECK_CREATE)
    @PostMapping("/rooms/{roomId}/daily-greeting")
    public BaseResponse<AttendanceCheckCreateResponse> createAttendanceCheck(
            @RequestBody @Valid final AttendanceCheckCreateRequest request,
            @Parameter(description = "오늘의 한마디를 작성할 방 id값") @PathVariable("roomId") final Long roomId,
            @Parameter(hidden = true) @UserId final Long userId) {
        return BaseResponse.ok(AttendanceCheckCreateResponse.of(
                attendanceCheckCreateUseCase.create(request.toCommand(userId, roomId))
        ));
    }

    @Operation(
            summary = "오늘의 한마디 삭제",
            description = "오늘의 한마디 작성자가 본인이 작성한 오늘의 한마디를 삭제합니다."
    )
    @ExceptionDescription(ATTENDANCE_CHECK_DELETE)
    @DeleteMapping("/rooms/{roomId}/daily-greeting/{attendanceCheckId}")
    public BaseResponse<AttendanceCheckDeleteResponse> deleteAttendanceCheck(
            @Parameter(description = "오늘의 한마디를 삭제할 방 id값") @PathVariable("roomId") final Long roomId,
            @Parameter(description = "삭제할 오늘의 한마디 id값") @PathVariable("attendanceCheckId") final Long attendanceCheckId,
            @Parameter(hidden = true) @UserId final Long userId) {
        return BaseResponse.ok(AttendanceCheckDeleteResponse.of(
                attendanceCheckDeleteUseCase.delete(userId, roomId, attendanceCheckId)
        ));
    }

    @Operation(
            summary = "오늘의 한마디 신고",
            description = "사용자가 오늘의 한마디를 신고합니다. 신고 횟수만 증가하며 별도의 처리는 이루어지지 않습니다."
    )
    @ExceptionDescription(ATTENDANCE_CHECK_REPORT)
    @PostMapping("/rooms/{roomId}/daily-greeting/{attendanceCheckId}/report")
    public BaseResponse<AttendanceCheckReportResponse> reportAttendanceCheck(
            @Parameter(description = "신고할 방 ID", example = "1") @PathVariable("roomId") final Long roomId,
            @Parameter(description = "신고하려는 오늘의 한마디 ID", example = "1") @PathVariable("attendanceCheckId") final Long attendanceCheckId) {
        return BaseResponse.ok(AttendanceCheckReportResponse.of(
                attendanceCheckReportUseCase.reportAttendanceCheck(attendanceCheckId)
        ));
    }

    @Operation(
            summary = "AI 기반 기록 독후감 생성",
            description = "AI를 활용하여 사용자가 작성한 기록을 바탕으로 독후감을 생성합니다."
    )
    @ExceptionDescription(RECORD_AI_REVIEW_CREATE)
    @PostMapping("/rooms/{roomId}/record/ai-review")
    public BaseResponse<RecordReviewCreateResponse> createAIRecordReview(
            @Parameter(hidden = true) @UserId final Long userId,
            @Parameter(description = "독후감을 생성할 방 ID", example = "1") @PathVariable final Long roomId
    ) {
        return BaseResponse.ok(
                RecordReviewCreateResponse.of(
                        recordReviewCreateUseCase.createAiRecordReview(roomId, userId)
                ));
    }
}
