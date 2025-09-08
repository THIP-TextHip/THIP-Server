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
    USER_SEARCH(new LinkedHashSet<>(Set.of(
            USER_NOT_FOUND
    ))),
    USER_UPDATE(new LinkedHashSet<>(Set.of(
            USER_NOT_FOUND,
            ALIAS_NAME_NOT_MATCH,
            USER_NICKNAME_TOO_LONG,
            USER_NICKNAME_CANNOT_BE_BLANK,
            USER_NICKNAME_CANNOT_BE_SAME,
            USER_NICKNAME_UPDATE_TOO_FREQUENT,
            USER_NICKNAME_ALREADY_EXISTS
    ))),
    USER_DELETE(new LinkedHashSet<>(Set.of(
            USER_CANNOT_DELETE_ROOM_HOST,
            USER_NOT_FOUND,
            USER_ALREADY_DELETED,
            USER_OAUTH2ID_CANNOT_BE_NULL
    ))),

    // Follow
    CHANGE_FOLLOW_STATE(new LinkedHashSet<>(Set.of(
            USER_NOT_FOUND,
            USER_ALREADY_FOLLOWED,
            USER_ALREADY_UNFOLLOWED,
            USER_CANNOT_FOLLOW_SELF,
            FOLLOW_COUNT_CANNOT_BE_NEGATIVE
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
            INVALID_ROOM_SEARCH_SORT,
            CATEGORY_NOT_MATCH
    ))),
    ROOM_PASSWORD_CHECK(new LinkedHashSet<>(Set.of(
            ROOM_NOT_FOUND,
            ROOM_RECRUITMENT_PERIOD_EXPIRED,
            ROOM_PASSWORD_NOT_REQUIRED
    ))),
    ROOM_RECRUITING_DETAIL_VIEW(new LinkedHashSet<>(Set.of(
            ROOM_NOT_FOUND,
            BOOK_NOT_FOUND,
            ROOM_RECRUITMENT_PERIOD_EXPIRED
    ))),
    ROOM_GET_HOME_JOINED_LIST(new LinkedHashSet<>(Set.of(
            USER_NOT_FOUND
    ))),
    ROOM_GET_MEMBER_LIST(new LinkedHashSet<>(Set.of(
            ROOM_NOT_FOUND
    ))),
    ROOM_PLAYING_DETAIL(new LinkedHashSet<>(Set.of(
            BOOK_NOT_FOUND,
            ROOM_NOT_FOUND,
            ROOM_ACCESS_FORBIDDEN
    ))),
    ROOM_GET_BOOK_PAGE(new LinkedHashSet<>(Set.of(
            ROOM_NOT_FOUND,
            BOOK_NOT_FOUND,
            ROOM_ACCESS_FORBIDDEN
    ))),
    ROOM_GET_DEADLINE_POPULAR(new LinkedHashSet<>(Set.of(
            CATEGORY_NOT_MATCH
    ))),
    CHANGE_ROOM_LIKE_STATE(new LinkedHashSet<>(Set.of(
            USER_NOT_FOUND,
            RECORD_NOT_FOUND,
            VOTE_NOT_FOUND,
            POST_ALREADY_LIKED,
            POST_NOT_LIKED_CANNOT_CANCEL,
            POST_LIKE_COUNT_UNDERFLOW,
            ROOM_ACCESS_FORBIDDEN,
            ROOM_POST_TYPE_NOT_MATCH
    ))),
    ROOM_LEAVE(new LinkedHashSet<>(Set.of(
            ROOM_NOT_FOUND,
            ROOM_PARTICIPANT_NOT_FOUND,
            ROOM_HOST_CANNOT_LEAVE
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
            ROOM_ACCESS_FORBIDDEN
    ))),
    RECORD_DELETE(new LinkedHashSet<>(Set.of(
            ROOM_ACCESS_FORBIDDEN,
            RECORD_NOT_FOUND,
            RECORD_ACCESS_FORBIDDEN
    ))),
    RECORD_PIN(new LinkedHashSet<>(Set.of(
            ROOM_ACCESS_FORBIDDEN,
            BOOK_NOT_FOUND,
            RECORD_NOT_FOUND,
            RECORD_ACCESS_FORBIDDEN
    ))),
    RECORD_UPDATE(new LinkedHashSet<>(Set.of(
            ROOM_ACCESS_FORBIDDEN,
            RECORD_NOT_FOUND,
            RECORD_ACCESS_FORBIDDEN
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
    VOTE(new LinkedHashSet<>(Set.of(
            ROOM_ACCESS_FORBIDDEN,
            VOTE_ITEM_NOT_FOUND,
            VOTE_ITEM_ALREADY_VOTED,
            VOTE_ITEM_NOT_VOTED_CANNOT_CANCEL,
            VOTE_ITEM_COUNT_CANNOT_BE_NEGATIVE
    ))),
    VOTE_DELETE(new LinkedHashSet<>(Set.of(
            ROOM_ACCESS_FORBIDDEN,
            VOTE_NOT_FOUND,
            VOTE_ACCESS_FORBIDDEN
    ))),
    VOTE_UPDATE(new LinkedHashSet<>(Set.of(
            ROOM_ACCESS_FORBIDDEN,
            VOTE_NOT_FOUND,
            VOTE_ACCESS_FORBIDDEN
    ))),


    // FEED
    FEED_CREATE(new LinkedHashSet<>(Set.of(
            USER_NOT_FOUND,
            BOOK_NOT_FOUND,
            TAG_NOT_FOUND,
            TAG_NAME_NOT_MATCH,
            TAG_SHOULD_BE_UNIQUE,
            TAG_LIST_SIZE_OVERFLOW,
            BOOK_NAVER_API_PARSING_ERROR,
            URL_INVALID_DOMAIN,
            URL_USER_ID_MISMATCH
    ))),
    FEED_IMAGE_UPLOAD(new LinkedHashSet<>(Set.of(
            INVALID_FILE_EXTENSION,
            FILE_SIZE_OVERFLOW,
            CONTENT_LIST_SIZE_OVERFLOW
    ))),
    FEED_UPDATE(new LinkedHashSet<>(Set.of(
            USER_NOT_FOUND,
            FEED_NOT_FOUND,
            BOOK_NOT_FOUND,
            TAG_NOT_FOUND,
            TAG_NAME_NOT_MATCH,
            CONTENT_LIST_SIZE_OVERFLOW,
            TAG_SHOULD_BE_UNIQUE,
            TAG_LIST_SIZE_OVERFLOW,
            FEED_ACCESS_FORBIDDEN
    ))),
    CHANGE_FEED_SAVED_STATE(new LinkedHashSet<>(Set.of(
            USER_NOT_FOUND,
            FEED_NOT_FOUND,
            FEED_ALREADY_SAVED,
            FEED_NOT_SAVED_CANNOT_DELETE
    ))),
    SHOW_SINGLE_FEED(new LinkedHashSet<>(Set.of(
            FEED_NOT_FOUND,
            USER_NOT_FOUND,
            BOOK_NOT_FOUND,
            FEED_CAN_NOT_SHOW_PRIVATE_ONE
    ))),
    CHANGE_FEED_LIKE_STATE(new LinkedHashSet<>(Set.of(
            USER_NOT_FOUND,
            FEED_NOT_FOUND,
            POST_ALREADY_LIKED,
            POST_NOT_LIKED_CANNOT_CANCEL,
            POST_LIKE_COUNT_UNDERFLOW,
            FEED_ACCESS_FORBIDDEN
    ))),
    FEED_DELETE(new LinkedHashSet<>(Set.of(
            FEED_NOT_FOUND,
            FEED_ACCESS_FORBIDDEN
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
            COMMENT_LIKE_COUNT_UNDERFLOW,
            FEED_ACCESS_FORBIDDEN,
            ROOM_ACCESS_FORBIDDEN
    ))),
    COMMENT_DELETE(new LinkedHashSet<>(Set.of(
            USER_NOT_FOUND,
            COMMENT_NOT_FOUND,
            FEED_NOT_FOUND,
            RECORD_NOT_FOUND,
            VOTE_NOT_FOUND,
            COMMENT_DELETE_FORBIDDEN,
            COMMENT_COUNT_UNDERFLOW,
            FEED_ACCESS_FORBIDDEN,
            ROOM_ACCESS_FORBIDDEN
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
    BOOK_SELECTABLE_LIST(new LinkedHashSet<>(Set.of(
            USER_NOT_FOUND
    ))),

    // Recent Search
    RECENT_SEARCH_DELETE(new LinkedHashSet<>(Set.of(
            RECENT_SEARCH_NOT_FOUND,
            RECENT_SEARCH_NOT_ADDED_BY_USER
    ))),

    // Attendance Check
    ATTENDANCE_CHECK_CREATE(new LinkedHashSet<>(Set.of(
            ROOM_ACCESS_FORBIDDEN,
            ROOM_NOT_FOUND,
            USER_NOT_FOUND,
            ATTENDANCE_CHECK_WRITE_LIMIT_EXCEEDED,
            ATTENDANCE_CHECK_NOT_FOUND
    ))),

    ATTENDANCE_CHECK_SHOW(new LinkedHashSet<>(Set.of(
            ROOM_ACCESS_FORBIDDEN
    ))),

    ATTENDANCE_CHECK_DELETE(new LinkedHashSet<>(Set.of(
            ROOM_ACCESS_FORBIDDEN,
            ATTENDANCE_CHECK_NOT_FOUND,
            ATTENDANCE_CHECK_CAN_NOT_DELETE
    ))),

    // Notiification
    FCM_TOKEN_REGISTER(new LinkedHashSet<>(Set.of(
            USER_NOT_FOUND,
            FCM_TOKEN_NOT_FOUND
    ))),
    FCM_TOKEN_ENABLE_STATE_CHANGE(new LinkedHashSet<>(Set.of(
            USER_NOT_FOUND,
            FCM_TOKEN_NOT_FOUND,
            FCM_TOKEN_ENABLED_STATE_ALREADY,
            FCM_TOKEN_ACCESS_FORBIDDEN
    ))),
    FCM_TOKEN_DELETE(new LinkedHashSet<>(Set.of(
            FCM_TOKEN_NOT_FOUND,
            FCM_TOKEN_ACCESS_FORBIDDEN
    )))

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
//                AUTH_BLACKLIST_TOKEN,

//                JSON_PROCESSING_ERROR
        )));


        this.errorCodeList = errorCodeList;
    }
}
