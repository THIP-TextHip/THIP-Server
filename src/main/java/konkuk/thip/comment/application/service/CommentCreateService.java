package konkuk.thip.comment.application.service;

import konkuk.thip.comment.adapter.in.web.response.CommentCreateResponse;
import konkuk.thip.comment.application.mapper.CommentQueryMapper;
import konkuk.thip.comment.application.port.in.CommentCreateUseCase;
import konkuk.thip.comment.application.port.in.dto.CommentCreateCommand;
import konkuk.thip.comment.application.port.out.CommentCommandPort;
import konkuk.thip.comment.application.port.out.CommentLikeQueryPort;
import konkuk.thip.comment.application.port.out.CommentQueryPort;
import konkuk.thip.comment.application.port.out.dto.CommentQueryDto;
import konkuk.thip.comment.application.service.validator.CommentAuthorizationValidator;
import konkuk.thip.comment.domain.Comment;
import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.common.post.CountUpdatable;
import konkuk.thip.common.post.service.PostHandler;
import konkuk.thip.common.post.PostType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static konkuk.thip.common.exception.code.ErrorCode.INVALID_COMMENT_CREATE;


@Service
@RequiredArgsConstructor
public class CommentCreateService implements CommentCreateUseCase {

    private final CommentCommandPort commentCommandPort;
    private final CommentQueryPort commentQueryPort;
    private final CommentLikeQueryPort commentLikeQueryPort;
    private final CommentQueryMapper commentQueryMapper;


    private final PostHandler postHandler;
    private final CommentAuthorizationValidator commentAuthorizationValidator;

    @Override
    @Transactional
    public CommentCreateResponse createComment(CommentCreateCommand command) {

        // 1. 댓글/답글 생성 선행검증 및 작성하려는 게시글 타입 검증
        Comment.validateCommentCreate(command.isReplyRequest(), command.parentId());
        PostType type = PostType.from(command.postType());

        // 2. 게시물 타입에 맞게 조회
        CountUpdatable post = postHandler.findPost(type, command.postId());
        // 2-1. 게시글 타입에 따른 댓글 생성 권한 검증
        commentAuthorizationValidator.validateUserCanAccessPostForComment(type, post, command.userId());

        // TODO 피드: 내 게시글의 댓글, 내 댓글에 대한 답글 알림 전송
        // TODO 기록 및 투표: 모임방의 내 게시글에 대한 댓글, 내 댓글에 대한 답글 알림 전송

        // 3. 댓글 생성
        Long savedCommentId = createCommentDomain(command);

        //TODO 게시물의 댓글 수 증가/감소 동시성 제어 로직 추가해야됨

        // 4. 게시글 댓글 수 증가
        // 4-1. 도메인 게시물 댓글 수 증가
        post.increaseCommentCount();
        // 4-2 Jpa엔티티 게시물 댓글 수 증가
        postHandler.updatePost(type, post);

        // 5. 매퍼로 DTO 변환 후 반환
        if (command.isReplyRequest()) {
            // 부모 댓글 조회
            CommentQueryDto parentCommentDto = commentQueryPort.findRootCommentById(command.parentId());
            // 사용자 부모 댓글 좋아요 여부 조회
            boolean isLikedParentComment = commentLikeQueryPort.isLikedCommentByUser(command.userId(),parentCommentDto.commentId());

            CommentQueryDto savedReplyCommentDto = commentQueryPort.findChildCommentById(command.parentId(), savedCommentId);
            return commentQueryMapper.toRootCommentResponseWithChildren(parentCommentDto, savedReplyCommentDto,isLikedParentComment,command.userId());
        } else {
            CommentQueryDto savedCommentDto = commentQueryPort.findRootCommentById(savedCommentId);
            return commentQueryMapper.toRoot(savedCommentDto, false, command.userId());
        }

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

}
