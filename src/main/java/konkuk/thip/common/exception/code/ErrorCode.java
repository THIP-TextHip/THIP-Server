package konkuk.thip.common.exception.code;

import konkuk.thip.common.dto.ResponseCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode implements ResponseCode {

    API_NOT_FOUND(HttpStatus.NOT_FOUND, 40400, "요청한 API를 찾을 수 없습니다."),
    API_METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, 40500, "허용되지 않는 HTTP 메소드입니다."),
    API_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 50000, "서버 내부 오류입니다."),

    API_BAD_REQUEST(HttpStatus.BAD_REQUEST, 40000, "잘못된 요청입니다."),
    API_MISSING_PARAM(HttpStatus.BAD_REQUEST, 40001, "필수 파라미터가 없습니다."),
    API_INVALID_PARAM(HttpStatus.BAD_REQUEST, 40002, "파라미터 값 중 유효하지 않은 값이 있습니다."),
    API_INVALID_TYPE(HttpStatus.BAD_REQUEST, 40003, "파라미터 타입이 잘못되었습니다."),

    AUTH_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, 40100, "유효하지 않은 토큰입니다."),
    AUTH_EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, 40101, "만료된 토큰입니다."),
    AUTH_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, 40102, "인증되지 않은 사용자입니다."),
    AUTH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, 40103, "토큰을 찾을 수 없습니다."),
    AUTH_LOGIN_FAILED(HttpStatus.UNAUTHORIZED, 40104, "로그인에 실패했습니다."),
    AUTH_UNSUPPORTED_SOCIAL_LOGIN(HttpStatus.UNAUTHORIZED, 40105, "지원하지 않는 소셜 로그인입니다."),
    AUTH_INVALID_LOGIN_TOKEN_KEY(HttpStatus.UNAUTHORIZED, 40106, "유효하지 않은 로그인 토큰 키입니다."),

    JSON_PROCESSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 50100, "JSON 직렬화/역직렬화에 실패했습니다."),
    AWS_BUCKET_BASE_URL_NOT_CONFIGURED(HttpStatus.INTERNAL_SERVER_ERROR, 50101, "aws s3 bucket base url 설정이 누락되었습니다."),

    PERSISTENCE_TRANSACTION_REQUIRED(HttpStatus.INTERNAL_SERVER_ERROR, 50110, "@Transactional 컨텍스트가 필요합니다. 트랜잭션 범위 내에서만 사용할 수 있습니다."),

    /* 60000부터 비즈니스 예외 */
    /**
     * 60000 : alias error
     */
    ALIAS_NOT_FOUND(HttpStatus.NOT_FOUND, 60001, "존재하지 않는 ALIAS 입니다."),
    ALIAS_NAME_NOT_MATCH(HttpStatus.BAD_REQUEST, 60002, "일치하는 칭호 이름이 없습니다."),

    /**
     * 70000 : user error
     */
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, 70000, "존재하지 않는 USER 입니다."),
    USER_ALREADY_FOLLOWED(HttpStatus.BAD_REQUEST, 70001, "이미 팔로우한 사용자입니다."),
    USER_NICKNAME_TOO_LONG(HttpStatus.BAD_REQUEST, 70002, "사용자 닉네임은 10자 이하여야 합니다."),
    USER_NICKNAME_CANNOT_BE_BLANK(HttpStatus.BAD_REQUEST, 70003, "사용자 닉네임은 비어있을 수 없습니다."),
    USER_NICKNAME_CANNOT_BE_SAME(HttpStatus.BAD_REQUEST, 70004, "사용자 닉네임은 이전과 동일할 수 없습니다."),
    USER_NICKNAME_UPDATE_TOO_FREQUENT(HttpStatus.BAD_REQUEST, 70005, "사용자 닉네임은 6개월에 한번 변경할 수 있습니다."),
    USER_NICKNAME_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, 70006, "다른 사용자가 이미 사용중인 닉네임입니다."),
    USER_ALREADY_SIGNED_UP(HttpStatus.BAD_REQUEST, 70007, "이미 가입된 사용자입니다."),
    USER_NOT_SIGNED_UP(HttpStatus.BAD_REQUEST, 70008, "가입되지 않은 사용자입니다."),

    /**
     * 75000 : follow error
     */
    FOLLOW_NOT_FOUND(HttpStatus.NOT_FOUND, 75000, "존재하지 않는 FOLLOW 입니다."),
    USER_ALREADY_UNFOLLOWED(HttpStatus.BAD_REQUEST, 75001, "이미 언팔로우한 사용자입니다."),
    USER_CANNOT_FOLLOW_SELF(HttpStatus.BAD_REQUEST, 75002, "사용자는 자신을 팔로우할 수 없습니다."),
    FOLLOW_COUNT_CANNOT_BE_NEGATIVE(HttpStatus.BAD_REQUEST, 75003, "사용자의 팔로우 수가 0일때는 언팔로우는 불가능합니다."),

    /**
     * 80000 : book error
     */
    BOOK_KEYWORD_ENCODING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 80000, "검색어 인코딩에 실패했습니다."),
    BOOK_NAVER_API_REQUEST_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 80001,"네이버 API 요청에 실패하였습니다."),
    BOOK_NAVER_API_PARSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 80002,"네이버 API 응답 파싱에 실패하였습니다."),
    BOOK_NAVER_API_URL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 80003,"네이버 API URL이 잘못되었습니다."),
    BOOK_NAVER_API_URL_HTTP_CONNECT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 80004,"네이버 API 요청 중, HTTP 연결에 실패하였습니다."),
    BOOK_NAVER_API_RESPONSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 80005,"네이버 API 응답에 실패하였습니다."),
    BOOK_SEARCH_PAGE_OUT_OF_RANGE(HttpStatus.BAD_REQUEST, 80006,"검색어 페이지가 범위를 벗어났습니다."),
    BOOK_KEYWORD_REQUIRED(HttpStatus.BAD_REQUEST, 80007, "검색어는 필수 입력값입니다."),
    BOOK_PAGE_NUMBER_INVALID(HttpStatus.BAD_REQUEST, 80008, "페이지 번호는 1 이상의 값이어야 합니다."),
    BOOK_NAVER_API_ISBN_NOT_FOUND(HttpStatus.BAD_REQUEST, 80009, "네이버 API 에서 ISBN으로 검색한 결과가 존재하지 않습니다."),
    BOOK_NOT_FOUND(HttpStatus.NOT_FOUND, 80010, "존재하지 않는 BOOK 입니다."),
    BOOK_ALREADY_SAVED(HttpStatus.BAD_REQUEST, 80011, "사용자가 이미 저장한 책입니다."),
    DUPLICATED_BOOKS_IN_COLLECTION(HttpStatus.INTERNAL_SERVER_ERROR, 80012, "중복된 책이 존재합니다."),
    BOOK_NOT_SAVED_CANNOT_DELETE(HttpStatus.BAD_REQUEST, 80013, "사용자가 저장하지 않은 책은 저장삭제 할 수 없습니다."),
    BOOK_NOT_SAVED_DB_CANNOT_DELETE(HttpStatus.BAD_REQUEST, 80014, "DB에 존재하지 않은 책은 저장삭제 할 수 없습니다."),
    BOOK_ALADIN_API_PARSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 80015, "알라딘 API 응답 파싱에 실패하였습니다."),
    BOOK_ALADIN_API_ISBN_NOT_FOUND(HttpStatus.BAD_REQUEST, 80016, "알라딘 API 에서 ISBN으로 검색한 결과가 존재하지 않습니다."),

    /**
     * 90000 : recentSearch error
     */
    INVALID_SEARCH_TYPE(HttpStatus.BAD_REQUEST, 90000,"알맞은 검색어 타입을 찾을 수 없습니다."),
    RECENT_SEARCH_NOT_FOUND(HttpStatus.NOT_FOUND, 90001, "존재하지 않는 RECENT SEARCH 입니다."),
    RECENT_SEARCH_NOT_ADDED_BY_USER(HttpStatus.BAD_REQUEST, 90002, "사용자가 추가하지 않은 검색어는 삭제할 수 없습니다."),

    /**
     * 100000 : room error
     */
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, 100000, "존재하지 않는 ROOM 입니다."),
    INVALID_ROOM_CREATE(HttpStatus.BAD_REQUEST, 100001, "유효하지 않은 ROOM 생성 요청 입니다."),
    ROOM_PASSWORD_NOT_REQUIRED(HttpStatus.BAD_REQUEST, 100003, "공개방은 비밀번호가 필요하지 않습니다."),
    ROOM_RECRUITMENT_PERIOD_EXPIRED(HttpStatus.BAD_REQUEST, 100004, "모집기간이 만료된 방입니다."),
    INVALID_ROOM_SEARCH_SORT(HttpStatus.BAD_REQUEST, 100005, "방 검색 시 정렬 조건이 잘못되었습니다."),
    ROOM_MEMBER_COUNT_EXCEEDED(HttpStatus.BAD_REQUEST, 100006, "방의 최대 인원 수를 초과했습니다."),
    ROOM_MEMBER_COUNT_UNDERFLOW(HttpStatus.BAD_REQUEST, 100007, "방의 인원 수가 1 이하(방장 포함)입니다."),
    ROOM_IS_EXPIRED(HttpStatus.BAD_REQUEST, 100008, "방이 만료되었습니다."),
    ROOM_POST_TYPE_NOT_MATCH(HttpStatus.BAD_REQUEST, 100009, "일치하는 방 게시물 타입 이름이 없습니다. [RECORD, VOTE] 중 하나여야 합니다."),

    /**
     * 110000 : vote error
     */
    VOTE_NOT_FOUND(HttpStatus.NOT_FOUND, 110000, "존재하지 않는 VOTE 입니다."),
    VOTE_CANNOT_BE_OVERVIEW(HttpStatus.BAD_REQUEST, 110001, "총평이 될 수 없는 VOTE 입니다."),
    INVALID_VOTE_PAGE_RANGE(HttpStatus.BAD_REQUEST, 110002, "VOTE의 page 값이 유효하지 않습니다."),
    VOTE_ACCESS_FORBIDDEN(HttpStatus.FORBIDDEN, 110003, "투표 접근 권한이 없습니다."),


    /**
     * 120000 : voteItem error
     */
    VOTE_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, 120000, "투표는 존재하지만 투표항목이 비어있습니다."),
    VOTE_ITEM_ALREADY_VOTED(HttpStatus.BAD_REQUEST, 120001, "이미 투표한 투표항목입니다."),
    VOTE_ITEM_NOT_VOTED_CANNOT_CANCEL(HttpStatus.BAD_REQUEST, 120002, "투표하지 않은 투표항목은 취소할 수 없습니다."),
    VOTE_ITEM_COUNT_CANNOT_BE_NEGATIVE(HttpStatus.BAD_REQUEST, 120003, "투표항목의 투표 수는 0 이하로 감소할 수 없습니다."),


    /**
     * 125000 : voteParticipant error
     */
    VOTE_PARTICIPANT_NOT_FOUND(HttpStatus.NOT_FOUND, 125000, "존재하지 않는 VOTE PARTICIPANT 입니다."),

    /**
     * 130000 : record error
     */
    RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, 130000, "존재하지 않는 RECORD 입니다."),
    RECORD_CANNOT_BE_OVERVIEW(HttpStatus.BAD_REQUEST, 130001, "총평이 될 수 없는 RECORD 입니다."),
    INVALID_RECORD_PAGE_RANGE(HttpStatus.BAD_REQUEST, 130002, "RECORD의 page 값이 유효하지 않습니다."),
    RECORD_ACCESS_FORBIDDEN(HttpStatus.FORBIDDEN, 130003, "기록 접근 권한이 없습니다."),

    /**
     * 140000 : roomParticipant error
     */
    ROOM_PARTICIPANT_NOT_FOUND(HttpStatus.NOT_FOUND, 140000, "존재하지 않는 RoomParticipant (방과 사용자 관계) 입니다."),
    ROOM_PARTICIPANT_ROLE_NOT_MATCH(HttpStatus.BAD_REQUEST, 140002, "일치하는 방에서의 사용자 역할이 없습니다."),
    ROOM_JOIN_TYPE_NOT_MATCH(HttpStatus.BAD_REQUEST, 140003, "일치하는 방 참여 상태가 없습니다."),
    USER_CANNOT_JOIN_OR_CANCEL(HttpStatus.BAD_REQUEST, 140004, "존재하지 않는 방은 참여하기 또는 취소하기가 불가능합니다."),
    USER_ALREADY_PARTICIPATE(HttpStatus.BAD_REQUEST, 140005, "사용자가 이미 방에 참여한 상태입니다."),
    USER_NOT_PARTICIPATED_CANNOT_CANCEL(HttpStatus.BAD_REQUEST, 140006, "사용자가 방에 참여하지 않은 상태에서 취소하기는 불가합니다."),
    HOST_CANNOT_CANCEL(HttpStatus.BAD_REQUEST, 140007, "방장은 참여 취소를 할 수 없습니다."),
    ROOM_RECRUIT_CANNOT_CLOSED(HttpStatus.BAD_REQUEST, 140008, "방 모집 마감을 할 수 없습니다."),
    INVALID_MY_ROOM_TYPE(HttpStatus.BAD_REQUEST, 140009, "유저가 참가한 방 목록 검색 요청에 유효하지 않은 MY ROOM type 이 있습니다."),
    INVALID_MY_ROOM_CURSOR(HttpStatus.BAD_REQUEST, 140010, "유저가 참가한 방 목록 검색 요청에 유효하지 않은 cursor 가 있습니다"),
    ROOM_ACCESS_FORBIDDEN(HttpStatus.FORBIDDEN, 140011, "방 접근 권한이 없습니다."),
    ROOM_HOST_CANNOT_LEAVE(HttpStatus.BAD_REQUEST, 140012, "방장은 방을 나갈 수 없습니다."),
    /**
     * 150000 : Category error
     */
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, 150000, "존재하지 않는 CATEGORY 입니다."),
    CATEGORY_NOT_MATCH(HttpStatus.BAD_REQUEST, 150001, "일치하는 카테고리 이름이 없습니다."),

    /**
     * 160000 : Feed,Tag error
     */
    FEED_NOT_FOUND(HttpStatus.NOT_FOUND, 160000, "존재하지 않는 FEED 입니다."),
    TAG_NAME_NOT_MATCH(HttpStatus.BAD_REQUEST, 160001, "일치하는 태그 이름이 없습니다."),
    TAG_NOT_FOUND(HttpStatus.NOT_FOUND, 160002, "존재하지 않는 TAG 입니다."),
    INVALID_FEED_COMMAND(HttpStatus.BAD_REQUEST, 160003, "유효하지 않은 FEED 생성/수정 요청 입니다."),
    FEED_ACCESS_FORBIDDEN(HttpStatus.FORBIDDEN, 160004, "피드 접근 권한이 없습니다."),
    FEED_ALREADY_SAVED(HttpStatus.BAD_REQUEST, 160006, "사용자가 이미 저장한 피드입니다."),
    FEED_NOT_SAVED_CANNOT_DELETE(HttpStatus.BAD_REQUEST, 160007, "사용자가 저장하지 않은 피드는 저장삭제 할 수 없습니다."),
    FEED_CAN_NOT_SHOW_PRIVATE_ONE(HttpStatus.BAD_REQUEST, 160008, "비공개 피드는 피드 작성자 이외에는 조회할 수 없습니다."),
    TAG_SHOULD_BE_UNIQUE(HttpStatus.BAD_REQUEST, 160009, "피드에 등록된 태그는 중복될 수 없습니다."),
    TAG_LIST_SIZE_OVERFLOW(HttpStatus.BAD_REQUEST, 160010, "등록 가능한 태그 개수를 초과하였습니다."),


    /**
     * 165000: ContentList error
     */
    CONTENT_LIST_SIZE_OVERFLOW(HttpStatus.BAD_REQUEST, 165000, "컨텐츠 리스트의 크기가 초과되었습니다. 최대 3개까지 가능합니다."),
    CONTENT_NOT_FOUND(HttpStatus.BAD_REQUEST, 165001, "해당 이미지는 이 피드에 존재하지 않습니다:"),

    /**
     * 170000 : Image File error
     */
    INVALID_FILE_EXTENSION(HttpStatus.BAD_REQUEST, 170001, "허용하지 않는 파일 확장자입니다."),
    FILE_SIZE_OVERFLOW(HttpStatus.BAD_REQUEST, 170002, "파일 크기가 허용 범위를 초과했습니다."),
    URL_INVALID_DOMAIN(HttpStatus.BAD_REQUEST, 170003, "잘못된 이미지 URL 형식입니다."),
    URL_USER_ID_MISMATCH(HttpStatus.BAD_REQUEST, 170004, "URL의 사용자 ID가 요청 사용자와 일치하지 않습니다."),

    /**
     * 180000 : Post error
     */
    POST_TYPE_NOT_MATCH(HttpStatus.BAD_REQUEST, 180000, "일치하는 게시물 타입 이름이 없습니다. [FEED, RECORD, VOTE] 중 하나여야 합니다."),
    POST_ALREADY_DELETED(HttpStatus.BAD_REQUEST, 180001, "이미 삭제된 게시물 입니다."),

    /**
     * 185000 : PostLike error
     *
     */
    POST_LIKE_COUNT_UNDERFLOW(HttpStatus.BAD_REQUEST, 185000, "좋아요 수는 0 이하로 감소할 수 없습니다."),
    POST_ALREADY_LIKED(HttpStatus.BAD_REQUEST, 185001, "사용자가 이미 좋아요한 게시물입니다."),
    POST_NOT_LIKED_CANNOT_CANCEL(HttpStatus.BAD_REQUEST, 185002, "사용자가 좋아요하지 않은 게시물은 좋아요 취소 할 수 없습니다."),

    /**
     * 190000 : Comment error
     */
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, 190000, "존재하지 않는 COMMENT 입니다."),
    INVALID_COMMENT_CREATE(HttpStatus.BAD_REQUEST, 190001, "유효하지 않은 COMMENT 생성 요청 입니다."),
    COMMENT_LIKE_COUNT_UNDERFLOW(HttpStatus.BAD_REQUEST, 190002, "좋아요 수는 0 이하로 감소할 수 없습니다."),
    COMMENT_ALREADY_LIKED(HttpStatus.BAD_REQUEST, 190003, "사용자가 이미 좋아요한 댓글입니다."),
    COMMENT_NOT_LIKED_CANNOT_CANCEL(HttpStatus.BAD_REQUEST, 190004, "사용자가 좋아요하지 않은 댓글은 좋아요 취소 할 수 없습니다."),
    COMMENT_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, 190005, "댓글 삭제 권한이 없습니다."),
    COMMENT_COUNT_UNDERFLOW(HttpStatus.BAD_REQUEST, 190007, "댓글 수는 0 이하로 감소할 수 없습니다."),

    /**
     * 195000 : AttendanceCheck error
     */
    ATTENDANCE_CHECK_WRITE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, 195000, "오늘의 한마디 작성 가능 횟수를 초과하였습니다."),
    ATTENDANCE_CHECK_NOT_FOUND(HttpStatus.NOT_FOUND, 195001, "존재하지 않는 ATTENDANCE CHECK 입니다."),
    ATTENDANCE_CHECK_CAN_NOT_DELETE(HttpStatus.FORBIDDEN, 195002, "오늘의 한마디는 본인만 삭제할 수 있습니다."),


    ;

    private final HttpStatus httpStatus;
    private final int code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, int code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
