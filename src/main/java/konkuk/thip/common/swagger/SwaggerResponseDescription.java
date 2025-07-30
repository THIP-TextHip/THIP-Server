package konkuk.thip.common.swagger;

import konkuk.thip.common.exception.code.ErrorCode;
import lombok.Getter;

import java.util.LinkedHashSet;
import java.util.Set;

import static konkuk.thip.common.exception.code.ErrorCode.*;

@Getter
public enum SwaggerResponseDescription {
//
    //Auth
    LOGIN(new LinkedHashSet<>(Set.of(
            USER_NOT_FOUND
    ))),
    LOGOUT(new LinkedHashSet<>(Set.of(

    ))),

    //User
    USER_SIGNUP(new LinkedHashSet<>(Set.of(
        ALIAS_NAME_NOT_MATCH
    ))),

    // Follow
    CHANGE_FOLLOW_STATE(new LinkedHashSet<>(Set.of(
            USER_NOT_FOUND,
            USER_ALREADY_FOLLOWED,
            USER_ALREADY_UNFOLLOWED,
            USER_CANNOT_FOLLOW_SELF,
            FOLLOW_COUNT_IS_ZERO
    ))),
    GET_USER_FOLLOW(new LinkedHashSet<>(Set.of(
            USER_NOT_FOUND
    ))),

    // Room
    ROOM_CREATE(new LinkedHashSet<>(Set.of(
            USER_NOT_FOUND,
            BOOK_ALADIN_API_PARSING_ERROR,
            BOOK_ALADIN_API_ISBN_NOT_FOUND

    ))),
    ROOM_JOIN_CANCEL(new LinkedHashSet<>(Set.of(
            USER_NOT_FOUND,
            ROOM_NOT_FOUND,
            ROOM_RECRUITMENT_PERIOD_EXPIRED,
            USER_CANNOT_JOIN_OR_CANCEL,
            ROOM_MEMBER_COUNT_EXCEEDED,
            USER_ALREADY_PARTICIPATE,
            ROOM_MEMBER_COUNT_UNDERFLOW,
            USER_NOT_PARTICIPATED_CANNOT_CANCEL,
            HOST_CANNOT_CANCEL
    ))),
    ROOM_RECRUIT_CLOSE(new LinkedHashSet<>(Set.of(
            USER_NOT_FOUND,
            ROOM_NOT_FOUND,
            ROOM_RECRUITMENT_PERIOD_EXPIRED,
            ROOM_RECRUIT_CANNOT_CLOSED
    ))),

    ROOM_SEARCH(new LinkedHashSet<>(Set.of(
                INVALID_ROOM_SEARCH_SORT
    ))),
    ROOM_PASSWORD_CHECK(new LinkedHashSet<>(Set.of(
            ROOM_NOT_FOUND,
            ROOM_PASSWORD_MISMATCH,
            ROOM_RECRUITMENT_PERIOD_EXPIRED,
            ROOM_PASSWORD_NOT_REQUIRED
    ))),
    ROOM_RECRUITING_DETAIL_VIEW(new LinkedHashSet<>(Set.of(
            ROOM_NOT_FOUND
    ))),
    ROOM_GET_HOME_JOINED_LIST(new LinkedHashSet<>(Set.of(
            USER_NOT_FOUND
    ))),
    ROOM_GET_MEMBER_LIST(new LinkedHashSet<>(Set.of(
            ROOM_NOT_FOUND
    ))),
    ROOM_PLAYING_DETAIL(new LinkedHashSet<>(Set.of(
            BOOK_NOT_FOUND,
            ROOM_NOT_FOUND
    ))),


    // Record
    RECORD_CREATE(new LinkedHashSet<>(Set.of(
            USER_NOT_FOUND,
            ROOM_NOT_FOUND,
            BOOK_NOT_FOUND,
            ROOM_IS_EXPIRED,
            RECORD_CANNOT_BE_OVERVIEW,
            INVALID_RECORD_PAGE_RANGE
    ))),
    RECORD_SEARCH(new LinkedHashSet<>(Set.of(
            USER_NOT_FOUND,
            ROOM_NOT_FOUND,
            BOOK_NOT_FOUND,
            USER_NOT_BELONG_TO_ROOM
    ))),

    // Vote
    VOTE_CREATE(new LinkedHashSet<>(Set.of(
            USER_NOT_FOUND,
            ROOM_NOT_FOUND,
            BOOK_NOT_FOUND,
            ROOM_IS_EXPIRED,
            VOTE_CANNOT_BE_OVERVIEW,
            INVALID_VOTE_PAGE_RANGE
    ))),

    // FEED
    FEED_CREATE(new LinkedHashSet<>(Set.of(
            USER_NOT_FOUND,
            BOOK_NOT_FOUND,
            TAG_NOT_FOUND,
            TAG_NAME_NOT_MATCH,
            INVALID_FEED_COMMAND,
            BOOK_NAVER_API_PARSING_ERROR,
            BOOK_NAVER_API_ISBN_NOT_FOUND,
            EMPTY_FILE_EXCEPTION,
            EXCEPTION_ON_IMAGE_UPLOAD,
            INVALID_FILE_EXTENSION
    ))),
    FEED_UPDATE(new LinkedHashSet<>(Set.of(
            USER_NOT_FOUND,
            FEED_NOT_FOUND,
            BOOK_NOT_FOUND,
            TAG_NOT_FOUND,
            TAG_NAME_NOT_MATCH,
            INVALID_FEED_COMMAND,
            FEED_ACCESS_FORBIDDEN
    ))),
    CHANGE_FEED_SAVED_STATE(new LinkedHashSet<>(Set.of(
            USER_NOT_FOUND,
            FEED_NOT_FOUND,
            FEED_ALREADY_SAVED,
            FEED_NOT_SAVED_CANNOT_DELETE
    ))),

    // Comment
    COMMENT_CREATE(new LinkedHashSet<>(Set.of(
            POST_TYPE_NOT_MATCH,
            USER_NOT_FOUND,
            FEED_NOT_FOUND,
            RECORD_NOT_FOUND,
            VOTE_NOT_FOUND,
            INVALID_COMMENT_CREATE,
            FEED_ACCESS_FORBIDDEN,
            ROOM_ACCESS_FORBIDDEN

    ))),
    CHANGE_COMMENT_LIKE_STATE(new LinkedHashSet<>(Set.of(
            USER_NOT_FOUND,
            COMMENT_NOT_FOUND,
            FEED_NOT_FOUND,
            RECORD_NOT_FOUND,
            VOTE_NOT_FOUND,
            COMMENT_ALREADY_LIKED,
            COMMENT_NOT_LIKED_CANNOT_CANCEL,
            COMMENT_LIKE_COUNT_UNDERFLOW
    ))),

    // Book
    CHANGE_BOOK_SAVED_STATE(new LinkedHashSet<>(Set.of(
            USER_NOT_FOUND,
            BOOK_NOT_FOUND,
            BOOK_ALREADY_SAVED,
            BOOK_NOT_SAVED_CANNOT_DELETE
//            DUPLICATED_BOOKS_IN_COLLECTION,
//            BOOK_NOT_SAVED_DB_CANNOT_DELETE
    ))),
    BOOK_SEARCH(new LinkedHashSet<>(Set.of(
            BOOK_SEARCH_PAGE_OUT_OF_RANGE,
            BOOK_KEYWORD_REQUIRED,
            BOOK_PAGE_NUMBER_INVALID,
            BOOK_NAVER_API_PARSING_ERROR,
            BOOK_NAVER_API_URL_HTTP_CONNECT_FAILED,
            BOOK_NAVER_API_RESPONSE_ERROR
    ))),
    BOOK_DETAIL_SEARCH(new LinkedHashSet<>(Set.of(
            BOOK_NOT_FOUND,
            BOOK_NAVER_API_PARSING_ERROR,
            BOOK_NAVER_API_ISBN_NOT_FOUND,
            BOOK_NAVER_API_URL_HTTP_CONNECT_FAILED
    ))),
    POPULAR_BOOK_SEARCH(new LinkedHashSet<>(Set.of(
            USER_NOT_FOUND,
            JSON_PROCESSING_ERROR
    ))),

  ;
    private final Set<ErrorCode> errorCodeList;
    SwaggerResponseDescription(Set<ErrorCode> errorCodeList) {
        // 공통 에러
        errorCodeList.addAll(new LinkedHashSet<>(Set.of(
                API_NOT_FOUND,
                API_METHOD_NOT_ALLOWED,
                API_SERVER_ERROR,

                API_MISSING_PARAM,
                API_INVALID_PARAM,
                API_INVALID_TYPE

//                AUTH_INVALID_TOKEN,
//                AUTH_EXPIRED_TOKEN,
//                AUTH_UNAUTHORIZED,
//                AUTH_TOKEN_NOT_FOUND
//                AUTH_LOGIN_FAILED,
//                AUTH_UNSUPPORTED_SOCIAL_LOGIN,

//                JSON_PROCESSING_ERROR
        )));


        this.errorCodeList = errorCodeList;
    }
}
