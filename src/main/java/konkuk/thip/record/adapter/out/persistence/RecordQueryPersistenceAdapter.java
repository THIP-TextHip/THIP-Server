package konkuk.thip.record.adapter.out.persistence;

import konkuk.thip.record.adapter.out.mapper.RecordMapper;
import konkuk.thip.record.application.port.in.dto.RecordSearchResult;
import konkuk.thip.record.application.port.out.RecordQueryPort;
import konkuk.thip.record.domain.Record;
import konkuk.thip.vote.adapter.out.mapper.VoteMapper;
import konkuk.thip.vote.adapter.out.persistence.VoteJpaRepository;
import konkuk.thip.vote.domain.Vote;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class RecordQueryPersistenceAdapter implements RecordQueryPort {

    private final RecordJpaRepository recordJpaRepository;
    private final VoteJpaRepository voteJpaRepository;
    private final RecordMapper recordMapper;
    private final VoteMapper voteMapper;

    private static final Integer PAGE_SIZE = 10;

    @Override
    public RecordSearchResult findRecordsByRoom(Long roomId, String type, Integer pageStart, Integer pageEnd, Long userId, Integer pageNum) {
        List<Record> records = recordJpaRepository.findRecordsByRoom(roomId, type, pageStart, pageEnd, userId).stream()
                .map(recordMapper::toDomainEntity)
                .toList();

        List<Vote> votes = voteJpaRepository.findVotesByRoom(roomId, type, pageStart, pageEnd, userId).stream()
                .map(voteMapper::toDomainEntity)
                .toList();

        return RecordSearchResult.of(records, votes);
    }
}

