package konkuk.thip.record.application.port.in.dto;

import konkuk.thip.record.domain.Record;
import konkuk.thip.vote.domain.Vote;
import lombok.Builder;

import java.util.List;

@Builder
public record RecordSearchResult(
        List<Record> records,
        List<Vote> votes
) {
    public static RecordSearchResult of(
            List<Record> records,
            List<Vote> votes
    ) {
        return RecordSearchResult.builder()
                .records(records)
                .votes(votes)
                .build();
    }
}
