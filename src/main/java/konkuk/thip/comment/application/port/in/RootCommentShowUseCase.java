package konkuk.thip.comment.application.port.in;

import konkuk.thip.comment.adapter.in.web.response.RootCommentsResponse;
import konkuk.thip.comment.application.port.in.dto.CommentShowAllQuery;

public interface RootCommentShowUseCase {

    RootCommentsResponse showRootCommentsOfPost(CommentShowAllQuery query);
}
