package konkuk.thip.post.application.port.in;

import konkuk.thip.post.application.port.in.dto.PostIsLikeCommand;
import konkuk.thip.post.application.port.in.dto.PostIsLikeResult;

public interface PostLikeUseCase {
    PostIsLikeResult changeLikeStatusPost(PostIsLikeCommand PostIsLikeCommand);
}