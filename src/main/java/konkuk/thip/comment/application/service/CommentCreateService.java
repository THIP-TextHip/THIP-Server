package konkuk.thip.comment.application.service;

import konkuk.thip.comment.application.port.in.CommentCreateUseCase;
import konkuk.thip.comment.application.port.in.dto.CommentCreateCommand;
import konkuk.thip.comment.application.port.out.CommentCommandPort;
import konkuk.thip.comment.application.service.validator.CommentAuthorizationValidator;
import konkuk.thip.comment.domain.Comment;
import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.common.post.CommentCountUpdatable;
import konkuk.thip.common.post.service.PostQueryService;
import konkuk.thip.feed.application.port.out.FeedCommandPort;
import konkuk.thip.feed.domain.Feed;
import konkuk.thip.common.post.PostType;
import konkuk.thip.record.application.port.out.RecordCommandPort;
import konkuk.thip.record.domain.Record;
import konkuk.thip.vote.application.port.out.VoteCommandPort;
import konkuk.thip.vote.domain.Vote;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static konkuk.thip.common.exception.code.ErrorCode.INVALID_COMMENT_CREATE;


@Service
@RequiredArgsConstructor
public class CommentCreateService implements CommentCreateUseCase {

    private final CommentCommandPort commentCommandPort;
    private final FeedCommandPort feedCommandPort;
    private final RecordCommandPort recordCommandPort;
    private final VoteCommandPort voteCommandPort;

    private final PostQueryService postQueryService;
    private final CommentAuthorizationValidator commentAuthorizationValidator;

    @Override
    @Transactional
    public Long createComment(CommentCreateCommand command) {

        // 1. 댓글/답글 생성 선행검증 및 작성하려는 게시글 타입 검증
        Comment.validateCommentCreate(command.isReplyRequest(), command.parentId());
        PostType type = PostType.from(command.postType());

        // 2. 게시물 타입에 맞게 조회
        CommentCountUpdatable post = postQueryService.findPost(type, command.postId());
        // 2-1. 게시글 타입에 따른 댓글 생성 권한 검증
        commentAuthorizationValidator.validateUserCanAccessPostForComment(type, post, command.userId());

        // TODO 피드: 내 게시글의 댓글, 내 댓글에 대한 답글 알림 전송
        // TODO 기록 및 투표: 모임방의 내 게시글에 대한 댓글, 내 댓글에 대한 답글 알림 전송

        // 3. 댓글 생성
        Long commentId = createCommentDomain(command);

        // 4. 게시글 댓글 수 증가
        // 4-1. 도메인 게시물 댓글 수 증가
        post.increaseCommentCount();
        // 4-2 Jpa엔티티 게시물 댓글 수 증가
        updatePost(type, post);

        return commentId;
    }

    private Long createCommentDomain(CommentCreateCommand command) {

        // 3-1. (답글일 경우) 부모 댓글 조회
        Comment parentComment = null;
        if (command.isReplyRequest()) {
            parentComment = commentCommandPort.findById(command.parentId()).orElseThrow(()
                    -> new InvalidStateException(INVALID_COMMENT_CREATE, new IllegalArgumentException("parentId에 해당하는 부모 댓글이 존재해야 합니다.")));
        }

        // 3-2. 도메인 댓글 생성 (유효성 검증 포함됨)
        Comment comment = Comment.createComment(
                command.content(),
                command.postId(),
                command.userId(),
                command.postType(),
                command.isReplyRequest(),
                command.parentId(),
                parentComment
        );

        return commentCommandPort.save(comment);
    }

    private void updatePost(PostType type, CommentCountUpdatable post) {
        switch (type) {
            case FEED -> feedCommandPort.update((Feed) post);
            case RECORD -> recordCommandPort.update((Record) post);
            case VOTE -> voteCommandPort.updateVote((Vote) post);
        }
    }

}
