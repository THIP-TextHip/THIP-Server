package konkuk.thip.vote.adapter.out.persistence;

import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.vote.adapter.out.jpa.VoteItemJpaEntity;
import konkuk.thip.vote.adapter.out.jpa.VoteJpaEntity;
import konkuk.thip.vote.adapter.out.jpa.VoteParticipantJpaEntity;
import konkuk.thip.vote.adapter.out.mapper.VoteItemMapper;
import konkuk.thip.vote.adapter.out.mapper.VoteMapper;
import konkuk.thip.vote.adapter.out.mapper.VoteParticipantMapper;
import konkuk.thip.vote.adapter.out.persistence.repository.VoteItemJpaRepository;
import konkuk.thip.vote.adapter.out.persistence.repository.VoteJpaRepository;
import konkuk.thip.vote.adapter.out.persistence.repository.VoteParticipantJpaRepository;
import konkuk.thip.vote.application.port.out.VoteCommandPort;
import konkuk.thip.vote.domain.Vote;
import konkuk.thip.vote.domain.VoteItem;
import konkuk.thip.vote.domain.VoteParticipant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static konkuk.thip.common.entity.StatusType.ACTIVE;
import static konkuk.thip.common.exception.code.ErrorCode.*;

@Repository
@RequiredArgsConstructor
public class VoteCommandPersistenceAdapter implements VoteCommandPort {

    private final VoteJpaRepository voteJpaRepository;
    private final VoteItemJpaRepository voteItemJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final RoomJpaRepository roomJpaRepository;
    private final VoteParticipantJpaRepository voteParticipantJpaRepository;

    private final VoteMapper voteMapper;
    private final VoteItemMapper voteItemMapper;
    private final VoteParticipantMapper voteParticipantMapper;

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
    public Optional<Vote> findById(Long id) {
        return voteJpaRepository.findByPostIdAndStatus(id,ACTIVE)
                .map(voteMapper::toDomainEntity);
    }

    @Override
    public Optional<VoteItem> findVoteItemById(Long id) {
        return voteItemJpaRepository.findById(id)
                .map(voteItemMapper::toDomainEntity);
    }

    @Override
    public Optional<VoteParticipant> findVoteParticipantByUserIdAndVoteId(Long userId, Long voteId) {
        return voteParticipantJpaRepository.findVoteParticipantByUserIdAndVoteId(userId, voteId)
                .map(voteParticipantMapper::toDomainEntity);
    }

    @Override
    public Optional<VoteParticipant> findVoteParticipantByUserIdAndVoteItemId(Long userId, Long voteItemId) {
        return voteParticipantJpaRepository.findVoteParticipantByUserIdAndVoteItemId(userId, voteItemId)
                .map(voteParticipantMapper::toDomainEntity);
    }

    @Override
    public void updateVoteParticipant(VoteParticipant voteParticipant) {
        VoteParticipantJpaEntity voteParticipantJpaEntity = voteParticipantJpaRepository.findById(voteParticipant.getId()).orElseThrow(
                () -> new EntityNotFoundException(VOTE_PARTICIPANT_NOT_FOUND)
        );

        VoteItemJpaEntity voteItemJpaEntity = voteItemJpaRepository.findById(voteParticipant.getVoteItemId()).orElseThrow(
                () -> new EntityNotFoundException(VOTE_ITEM_NOT_FOUND)
        );

        voteParticipantJpaRepository.save(voteParticipantJpaEntity.updateVoteItem(voteItemJpaEntity));
    }

    @Override
    public void saveVoteParticipant(VoteParticipant voteParticipant) {
        UserJpaEntity userJpaEntity = userJpaRepository.findById(voteParticipant.getUserId()).orElseThrow(
                () -> new EntityNotFoundException(USER_NOT_FOUND)
        );

        VoteItemJpaEntity voteItemJpaEntity = voteItemJpaRepository.findById(voteParticipant.getVoteItemId()).orElseThrow(
                () -> new EntityNotFoundException(VOTE_ITEM_NOT_FOUND)
        );

        VoteParticipantJpaEntity voteParticipantJpaEntity = voteParticipantMapper.toJpaEntity(userJpaEntity, voteItemJpaEntity);
        voteParticipantJpaRepository.save(voteParticipantJpaEntity);
    }

    @Override
    public void deleteVoteParticipant(VoteParticipant voteParticipant) {
        // 앞에서 이미 존재 여부를 확인했으므로, 여기서는 ID로 삭제
        voteParticipantJpaRepository.deleteById(voteParticipant.getId());
    }

    @Override
    public void updateVoteItem(VoteItem voteItem) {
        VoteItemJpaEntity voteItemJpaEntity = voteItemJpaRepository.findById(voteItem.getId()).orElseThrow(
                () -> new EntityNotFoundException(VOTE_ITEM_NOT_FOUND)
        );

        voteItemJpaRepository.save(voteItemJpaEntity.updateFrom(voteItem));
    }

    @Override
    public void delete(Vote vote) {
        VoteJpaEntity voteJpaEntity = voteJpaRepository.findById(vote.getId()).orElseThrow(
                () -> new EntityNotFoundException(VOTE_NOT_FOUND)
        );

        voteParticipantJpaRepository.deleteAllByVoteId(voteJpaEntity.getPostId());
        voteItemJpaRepository.deleteAllByVoteId(voteJpaEntity.getPostId());

        voteJpaEntity.softDelete();
        voteJpaRepository.save(voteJpaEntity);
    }


    @Override
    public void updateVote(Vote vote) {
        VoteJpaEntity voteJpaEntity = voteJpaRepository.findById(vote.getId()).orElseThrow(
                () -> new EntityNotFoundException(VOTE_NOT_FOUND)
        );

        voteJpaRepository.save(voteJpaEntity.updateFrom(vote));
    }

}
