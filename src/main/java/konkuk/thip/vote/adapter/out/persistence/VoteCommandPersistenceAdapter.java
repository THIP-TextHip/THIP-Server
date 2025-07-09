package konkuk.thip.vote.adapter.out.persistence;

import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.adapter.out.persistence.RoomJpaRepository;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.UserJpaRepository;
import konkuk.thip.vote.adapter.out.jpa.VoteItemJpaEntity;
import konkuk.thip.vote.adapter.out.jpa.VoteJpaEntity;
import konkuk.thip.vote.adapter.out.mapper.VoteItemMapper;
import konkuk.thip.vote.adapter.out.mapper.VoteMapper;
import konkuk.thip.vote.application.port.out.VoteCommandPort;
import konkuk.thip.vote.domain.Vote;
import konkuk.thip.vote.domain.VoteItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static konkuk.thip.common.exception.code.ErrorCode.*;

@Repository
@RequiredArgsConstructor
public class VoteCommandPersistenceAdapter implements VoteCommandPort {

    private final VoteJpaRepository voteJpaRepository;
    private final VoteItemJpaRepository voteItemJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final RoomJpaRepository roomJpaRepository;

    private final VoteMapper voteMapper;
    private final VoteItemMapper voteItemMapper;

    @Override
    public Long saveVote(Vote vote) {
        UserJpaEntity userJpaEntity = userJpaRepository.findById(vote.getCreatorId()).orElseThrow(
                () -> new EntityNotFoundException(USER_NOT_FOUND)
        );

        RoomJpaEntity roomJpaEntity = roomJpaRepository.findById(vote.getRoomId()).orElseThrow(
                () -> new EntityNotFoundException(ROOM_NOT_FOUND)
        );

        return voteJpaRepository.save(voteMapper.toJpaEntity(vote, userJpaEntity, roomJpaEntity)).getPostId();
    }

    @Override
    public void saveAllVoteItems(List<VoteItem> voteItems) {
        if (voteItems.isEmpty()) return;

        Long voteId = voteItems.get(0).getVoteId();
        VoteJpaEntity voteJpaEntity = voteJpaRepository.findById(voteId).orElseThrow(
                () -> new EntityNotFoundException(VOTE_NOT_FOUND)
        );

        List<VoteItemJpaEntity> voteItemJpaEntities = voteItems.stream()
                .map(voteItem -> voteItemMapper.toJpaEntity(voteItem, voteJpaEntity))
                .toList();

        voteItemJpaRepository.saveAll(voteItemJpaEntities);
    }

    @Override
    public List<VoteItem> findVoteItemsByVoteId(Long voteId) {
        List<VoteItem> voteItems = voteItemJpaRepository.findAllByVoteJpaEntity_PostId(voteId).stream()
                .map(voteItemMapper::toDomainEntity)
                .toList();

        if (voteItems.isEmpty()) {
            throw new InvalidStateException(VOTE_ITEM_NOT_FOUND);
        }

        return voteItems;
    }

}
