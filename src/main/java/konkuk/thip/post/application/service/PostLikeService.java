package konkuk.thip.post.application.service;

import jakarta.transaction.Transactional;
import konkuk.thip.common.post.CountUpdatable;
import konkuk.thip.common.post.service.PostHandler;
import konkuk.thip.post.application.port.in.dto.PostIsLikeCommand;
import konkuk.thip.post.application.port.in.dto.PostIsLikeResult;
import konkuk.thip.post.application.port.in.PostLikeUseCase;
import konkuk.thip.post.application.port.out.PostLikeCommandPort;
import konkuk.thip.post.application.port.out.PostLikeQueryPort;
import konkuk.thip.post.application.service.validator.PostLikeAuthorizationValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostLikeService implements PostLikeUseCase {

    private final PostLikeQueryPort postLikeQueryPort;
    private final PostLikeCommandPort postLikeCommandPort;

    private final PostHandler postHandler;
    private final PostLikeAuthorizationValidator postLikeAuthorizationValidator;

    @Override
    @Transactional
    public PostIsLikeResult changeLikeStatusPost(PostIsLikeCommand command) {

        // 1. 게시물 타입에 맞게 검증 및 조회
        CountUpdatable post = postHandler.findPost(command.postType(), command.postId());
        // 1-1. 게시글 타입에 따른 게시물 좋아요 권한 검증
        postLikeAuthorizationValidator.validateUserCanAccessPostLike(command.postType(), post, command.userId());

        // 2. 유저가 해당 게시물에 대해 좋아요 했는지 조회
        boolean alreadyLiked = postLikeQueryPort.isLikedPostByUser(command.userId(), command.postId());

        // 3. 좋아요 상태변경
        //TODO 게시물의 좋아요 수 증가/감소 동시성 제어 로직 추가해야됨
        if (command.isLike()) {
            postLikeAuthorizationValidator.validateUserCanLike(alreadyLiked); // 좋아요 가능 여부 검증
            postLikeCommandPort.save(command.userId(), command.postId(),command.postType());
        } else {
            postLikeAuthorizationValidator.validateUserCanUnLike(alreadyLiked); // 좋아요 취소 가능 여부 검증
            postLikeCommandPort.delete(command.userId(), command.postId());
        }

        // 4. 게시물 좋아요 수 업데이트
        post.updateLikeCount(command.isLike());
        postHandler.updatePost(command.postType(), post);

        return PostIsLikeResult.of(post.getId(), command.isLike());
    }
}
