package konkuk.thip.comment.application.port.in;

import konkuk.thip.comment.adapter.in.web.response.ChildCommentsResponse;
import konkuk.thip.comment.application.port.in.dto.ChildCommentsShowQuery;

public interface ChildCommentShowUseCase {
    ChildCommentsResponse showChildComments(ChildCommentsShowQuery query);
}

