package konkuk.thip.roompost.application.port.in;

import konkuk.thip.roompost.application.port.in.dto.record.RecordUpdateCommand;
import konkuk.thip.roompost.application.port.in.dto.vote.VoteUpdateCommand;

public interface RoomPostUpdateUseCase {
    Long updateRecord(RecordUpdateCommand command);
    Long updateVote(VoteUpdateCommand command);
}
