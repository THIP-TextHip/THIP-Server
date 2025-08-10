package konkuk.thip.user.application.service;

import jakarta.transaction.Transactional;
import konkuk.thip.comment.application.port.out.CommentCommandPort;
import konkuk.thip.comment.application.port.out.CommentLikeCommandPort;
import konkuk.thip.common.post.service.PostHandler;
import konkuk.thip.room.application.port.out.RoomCommandPort;
import konkuk.thip.room.application.port.out.RoomParticipantCommandPort;
import konkuk.thip.user.application.port.in.UserDeleteUseCase;
import konkuk.thip.user.application.port.out.UserCommandPort;
import konkuk.thip.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDeleteService implements UserDeleteUseCase {

    private final UserCommandPort userCommandPort;

    private final CommentCommandPort commentCommandPort;
    private final CommentLikeCommandPort commentLikeCommandPort;

    private final RoomParticipantCommandPort roomParticipantCommandPort;
    private final RoomCommandPort roomCommandPort;

    private final PostHandler postHandler;

    @Override
    @Transactional
    public void deleteUser(Long userId) {

        // 1. 유저 조회 및 검증
        User user = userCommandPort.getByIdOrThrow(userId);

        // 사용자가 댓글 남긴 게시글의 댓글 수 감소
        // 사용자가 좋아요 남긴 댓글의 좋아요 수 감소

        // 사용자가 좋아요 남긴 게시글의 좋아요 수 감소
        // 사용자가 투표한 항목의 투표 수 감소

        // 사용자가 참여한 방의 멤버 수 감소
        // 사용자가 방의 HOST라면 연관된 방 삭제

//        List<Comment> userComments = commentCommandPort.findAllByUserId(userId);
//
//        Map<PostType, List<Comment>> groupedComments = userComments.stream()
//                .collect(Collectors.groupingBy(Comment::getPostType));
//
//        for (Map.Entry<PostType, List<Comment>> entry : groupedComments.entrySet()) {
//            PostType postType = entry.getKey();
//            List<Comment> comments = entry.getValue();
//
//            // 포스트 ID별 삭제된 댓글 수 집계
//            Map<Long, Long> postIdToCount = comments.stream()
//                    .collect(Collectors.groupingBy(Comment::getTargetPostId, Collectors.counting()));
//
//            for (Map.Entry<Long, Long> pcEntry : postIdToCount.entrySet()) {
//                Long postId = pcEntry.getKey();
//                int decreaseCount = pcEntry.getValue().intValue();
//
//                // 헬퍼 서비스로 해당 postType, 포스트 단위 조회 및 업데이트
//                CountUpdatable post = postHandler.findPost(postType, postId);
//                post.decreaseCommentCount(decreaseCount);
//                postHandler.updatePost(postType, post);
//            }
//        }
//
//
//        // 게시글 ID별 댓글 삭제 개수 집계 (삭제된 댓글 수)
//        Map<PostType, List<Comment>> groupedComments = userComments.stream()
//                .collect(Collectors.groupingBy(Comment::getPostType));
//
//
//        // 게시글별 댓글 수 감소 처리만 수행
//        for (Map.Entry<Long, Long> entry : postCommentCountMap.entrySet()) {
//            postCommandPort.decreaseCommentCountBy(entry.getKey(), entry.getValue());
//        }
//
//        // 2. 사용자가 댓글 남긴 게시글의 댓글 수 감소
//        List<Comment> userComments = commentCommandPort.findAllByUserId(userId);
//        for (Comment comment : userComments) {
//            // 댓글 좋아요 삭제
//            commentLikeCommandPort.deleteAllByCommentId(comment.getId());
//
//            // 댓글 삭제 (소프트 딜리트 방식 가정)
//            commentCommandPort.delete(comment);
//
//            // 댓글의 게시글 댓글 수 감소
//            // 해당 게시글 조회
//            var post = postCommandPort.getByIdOrThrow(comment.getTargetPostId());
//            post.decreaseCommentCount();
//            postCommandPort.update(post);
//        }
//
//        // 3. 사용자가 좋아요 남긴 댓글의 좋아요 수 감소 및 좋아요 삭제
//        List<CommentLike> commentLikes = commentLikeCommandPort.findAllByUserId(userId);
//        for (CommentLike cl : commentLikes) {
//            var likedComment = commentCommandPort.getByIdOrThrow(cl.getCommentId());
//            likedComment.decreaseLikeCount();
//            commentCommandPort.update(likedComment);
//            commentLikeCommandPort.delete(cl);
//        }
//
//        // 4. 사용자가 좋아요 남긴 게시글의 좋아요 수 감소 및 좋아요 삭제
//        List<PostLike> postLikes = postLikeCommandPort.findAllByUserId(userId);
//        for (PostLike pl : postLikes) {
//            var likedPost = postCommandPort.getByIdOrThrow(pl.getPostId());
//            likedPost.decreaseLikeCount();
//            postCommandPort.update(likedPost);
//            postLikeCommandPort.delete(pl);
//        }
//
//        // 5. 사용자가 투표한 항목의 투표 수 감소 및 투표 참여 기록 삭제
//        List<VoteParticipant> voteParticipants = voteParticipantCommandPort.findAllByUserId(userId);
//        for (VoteParticipant vp : voteParticipants) {
//            var vote = voteParticipantCommandPort.getVoteById(vp.getVoteId());
//            vote.decreaseParticipantCount();
//            voteParticipantCommandPort.updateVote(vote);
//            voteParticipantCommandPort.delete(vp);
//        }
//
//        // 6. 사용자가 참여한 방의 멤버 수 감소 및 HOST면 방 삭제
//        List<RoomParticipant> roomParticipants = roomParticipantCommandPort.findAllByUserId(userId);
//        for (RoomParticipant rp : roomParticipants) {
//            Room room = roomCommandPort.getByIdOrThrow(rp.getRoomId());
//
//            // 방참여자 삭제
//            roomParticipantCommandPort.delete(rp);
//
//            // HOST 여부 판단
//            if (rp.getRole() == RoomParticipantRole.HOST) {
//                // HOST면 방 삭제
//                roomCommandPort.delete(room);
//            } else {
//                // 멤버 수 감소 및 저장
//                room.decreaseMemberCount();
//                roomCommandPort.update(room);
//            }
//        }
//
//        // 7. 마지막으로 유저 soft delete
//        userCommandPort.delete(user);
    }

}
