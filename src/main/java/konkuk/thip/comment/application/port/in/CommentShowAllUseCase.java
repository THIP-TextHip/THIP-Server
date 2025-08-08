package konkuk.thip.comment.application.port.in;

import konkuk.thip.comment.adapter.in.web.response.CommentForSinglePostResponse;
import konkuk.thip.comment.application.port.in.dto.CommentShowAllQuery;

public interface CommentShowAllUseCase {

    CommentForSinglePostResponse showAllCommentsOfPost(CommentShowAllQuery query);
}
