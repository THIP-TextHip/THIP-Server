package konkuk.thip.roompost.adapter.out.persistence;

import konkuk.thip.comment.adapter.out.persistence.repository.CommentJpaRepository;
import konkuk.thip.comment.adapter.out.persistence.repository.CommentLikeJpaRepository;
import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.post.adapter.out.persistence.repository.PostLikeJpaRepository;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.roompost.adapter.out.jpa.VoteItemJpaEntity;
import konkuk.thip.roompost.adapter.out.jpa.VoteJpaEntity;
import konkuk.thip.roompost.adapter.out.jpa.VoteParticipantJpaEntity;
import konkuk.thip.roompost.adapter.out.mapper.VoteItemMapper;
import konkuk.thip.roompost.adapter.out.mapper.VoteMapper;
import konkuk.thip.roompost.adapter.out.mapper.VoteParticipantMapper;
import konkuk.thip.roompost.adapter.out.persistence.repository.vote.VoteItemJpaRepository;
import konkuk.thip.roompost.adapter.out.persistence.repository.vote.VoteJpaRepository;
import konkuk.thip.roompost.adapter.out.persistence.repository.vote.VoteParticipantJpaRepository;
import konkuk.thip.roompost.application.port.out.VoteCommandPort;
import konkuk.thip.roompost.domain.Vote;
import konkuk.thip.roompost.domain.VoteItem;
import konkuk.thip.roompost.domain.VoteParticipant;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static konkuk.thip.common.exception.code.ErrorCode.*;

@Repository
@RequiredArgsConstructor
public class VoteCommandPersistenceAdapter implements VoteCommandPort {

    private final VoteJpaRepository voteJpaRepository;
    private final VoteItemJpaRepository voteItemJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final RoomJpaRepository roomJpaRepository;
    private final VoteParticipantJpaRepository voteParticipantJpaRepository;

    private final CommentJpaRepository commentJpaRepository;
    private final CommentLikeJpaRepository commentLikeJpaRepository;
    private final PostLikeJpaRepository postLikeJpaRepository;

    private final VoteMapper voteMapper;
    private final VoteItemMapper voteItemMapper;
    private final VoteParticipantMapper voteParticipantMapper;

    @Override
    public Long saveVote(Vote vote) {
        UserJpaEntity userJpaEntity = userJpaRepository.findByUserId(vote.getCreatorId()).orElseThrow(
                () -> new EntityNotFoundException(USER_NOT_FOUND)
        );

        RoomJpaEntity roomJpaEntity = roomJpaRepository.findByRoomId(vote.getRoomId()).orElseThrow(
                () -> new EntityNotFoundException(ROOM_NOT_FOUND)
        );

        return voteJpaRepository.save(voteMapper.toJpaEntity(vote, userJpaEntity, roomJpaEntity)).getPostId();
    }

    @Override
    public void saveAllVoteItems(List<VoteItem> voteItems) {
        if (voteItems.isEmpty()) return;

        Long voteId = voteItems.get(0).getVoteId();
        VoteJpaEntity voteJpaEntity = voteJpaRepository.findByPostId(voteId).orElseThrow(
                () -> new EntityNotFoundException(VOTE_NOT_FOUND)
        );

        List<VoteItemJpaEntity> voteItemJpaEntities = voteItems.stream()
                .map(voteItem -> voteItemMapper.toJpaEntity(voteItem, voteJpaEntity))
                .toList();

        voteItemJpaRepository.saveAll(voteItemJpaEntities);
    }

    @Override
    public Optional<Vote> findById(Long id) {
        return voteJpaRepository.findByPostId(id)
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
        UserJpaEntity userJpaEntity = userJpaRepository.findByUserId(voteParticipant.getUserId()).orElseThrow(
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
        VoteJpaEntity voteJpaEntity = voteJpaRepository.findByPostId(vote.getId()).orElseThrow(
                () -> new EntityNotFoundException(VOTE_NOT_FOUND)
        );

        voteParticipantJpaRepository.deleteAllByVoteId(voteJpaEntity.getPostId());
        voteItemJpaRepository.deleteAllByVoteId(voteJpaEntity.getPostId());

        voteJpaEntity.softDelete();
        voteJpaRepository.save(voteJpaEntity);
    }

    @Override
    public void deleteAllVoteParticipantByUserId(Long userId) {

        // 1. 탈퇴 유저가 참여한 모든 투표 항목 ID 조회
        List<Long> voteItemIds = voteParticipantJpaRepository.findAllVoteItemIdsByUserId(userId);
        if (voteItemIds == null || voteItemIds.isEmpty()) {
            return; //early return
        }
        // 2. 투표 참여 관계 삭제
        voteParticipantJpaRepository.deleteAllByUserId(userId);
        // 3. 탈퇴 유저가 투표 했던 투표 항목들의 득표 수 감소
        voteItemJpaRepository.bulkDecrementLikeCount(voteItemIds);
    }

    @Override
    public void deleteAllVoteByUserId(Long userId) {
        // 1. 유저가 작성한 투표 게시글 ID 리스트 조회
        List<Long> voteIds = voteJpaRepository.findVoteIdsByUserId(userId);
        if (voteIds == null || voteIds.isEmpty()) {
            return; // early return
        }
        // 2-1. 댓글 좋아요 일괄 삭제
        commentLikeJpaRepository.deleteAllByPostIds(voteIds);
        // 2-2. 댓글 soft delete 일괄 처리
        commentJpaRepository.softDeleteAllByPostIds(voteIds);
        // 3. 게시글 좋아요 일괄 삭제
        postLikeJpaRepository.deleteAllByPostIds(voteIds);
        // 4-1. 투표 참여 관계 일괄 삭제
        voteParticipantJpaRepository.deleteAllByVoteIds(voteIds);
        // 4-2. 투표 항목 일괄 삭제
        voteItemJpaRepository.deleteAllByVoteIds(voteIds);
        // 5. 탈퇴한 유저가 작성한 투표 soft delete 일괄 처리
        voteJpaRepository.softDeleteAllByUserId(userId);
    }


    @Override
    public void updateVote(Vote vote) {
        VoteJpaEntity voteJpaEntity = voteJpaRepository.findByPostId(vote.getId()).orElseThrow(
                () -> new EntityNotFoundException(VOTE_NOT_FOUND)
        );

        voteJpaRepository.save(voteJpaEntity.updateFrom(vote));
    }

}
