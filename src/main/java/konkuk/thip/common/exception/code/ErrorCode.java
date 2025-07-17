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

    JSON_PROCESSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 50100, "JSON 직렬화/역직렬화에 실패했습니다."),

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

    /**
     * 75000 : follow error
     */
    FOLLOW_NOT_FOUND(HttpStatus.NOT_FOUND, 75000, "존재하지 않는 FOLLOW 입니다."),
    USER_ALREADY_UNFOLLOWED(HttpStatus.BAD_REQUEST, 75001, "이미 언팔로우한 사용자입니다."),
    USER_CANNOT_FOLLOW_SELF(HttpStatus.BAD_REQUEST, 75002, "사용자는 자신을 팔로우할 수 없습니다."),
    FOLLOW_COUNT_IS_ZERO(HttpStatus.BAD_REQUEST, 75003, "사용자의 팔로우 수가 0일때는 언팔로우는 불가능합니다."),

    /**
     * 80000 : book error
     */
    BOOK_KEYWORD_ENCODING_FAILED(HttpStatus.BAD_REQUEST, 80000, "검색어 인코딩에 실패했습니다."),
    BOOK_NAVER_API_REQUEST_ERROR(HttpStatus.BAD_REQUEST, 80001,"네이버 API 요청에 실패하였습니다."),
    BOOK_NAVER_API_PARSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 80002,"네이버 API 응답 파싱에 실패하였습니다."),
    BOOK_NAVER_API_URL_ERROR(HttpStatus.BAD_REQUEST, 80003,"네이버 API URL이 잘못되었습니다."),
    BOOK_NAVER_API_URL_HTTP_CONNECT_FAILED(HttpStatus.BAD_REQUEST, 80004,"네이버 API 요청 중, HTTP 연결에 실패하였습니다."),
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

    /**
     * 100000 : room error
     */
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, 100000, "존재하지 않는 ROOM 입니다."),
    INVALID_ROOM_CREATE(HttpStatus.BAD_REQUEST, 100001, "유효하지 않은 ROOM 생성 요청 입니다."),
    ROOM_PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, 100002, "비밀번호가 일치하지 않습니다."),
    ROOM_PASSWORD_NOT_REQUIRED(HttpStatus.BAD_REQUEST, 100003, "공개방은 비밀번호가 필요하지 않습니다."),
    ROOM_RECRUITMENT_PERIOD_EXPIRED(HttpStatus.BAD_REQUEST, 100004, "모집기간이 만료된 방입니다."),
    INVALID_ROOM_SEARCH_SORT(HttpStatus.BAD_REQUEST, 100005, "방 검색 시 정렬 조건이 잘못되었습니다."),

    /**
     * 110000 : vote error
     */
    VOTE_NOT_FOUND(HttpStatus.NOT_FOUND, 110000, "존재하지 않는 VOTE 입니다."),
    VOTE_CANNOT_BE_OVERVIEW(HttpStatus.BAD_REQUEST, 110001, "총평이 될 수 없는 VOTE 입니다."),
    INVALID_VOTE_PAGE_RANGE(HttpStatus.BAD_REQUEST, 110002, "VOTE의 page 값이 유효하지 않습니다."),

    /**
     * 120000 : voteItem error
     */
    VOTE_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, 120000, "투표는 존재하지만 투표항목이 비어있습니다."),

    /**
     * 130000 : record error
     */
    RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, 130000, "존재하지 않는 RECORD 입니다."),
    RECORD_CANNOT_BE_OVERVIEW(HttpStatus.BAD_REQUEST, 130001, "총평이 될 수 없는 RECORD 입니다."),
    INVALID_RECORD_PAGE_RANGE(HttpStatus.BAD_REQUEST, 130002, "RECORD의 page 값이 유효하지 않습니다."),
    RECORD_CANNOT_WRITE_IN_EXPIRED_ROOM(HttpStatus.BAD_REQUEST, 120003, "만료된 방에는 기록을 남길 수 없습니다."),

    /**
     * 140000 : roomParticipant error
     */
    ROOM_PARTICIPANT_NOT_FOUND(HttpStatus.NOT_FOUND, 140000, "존재하지 않는 RoomParticipant (방과 사용자 관계) 입니다."),
    USER_NOT_BELONG_TO_ROOM(HttpStatus.BAD_REQUEST, 140001, "현재 모임방에 속하지 않는 유저입니다."),
    ROOM_PARTICIPANT_ROLE_NOT_MATCH(HttpStatus.BAD_REQUEST, 140002, "일치하는 방에서의 사용자 역할이 없습니다."),

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
    INVALID_FEED_CREATE(HttpStatus.BAD_REQUEST, 160003, "유효하지 않은 FEED 생성 요청 입니다."),

    /**
     * 170000 : Image File error
     */
    EMPTY_FILE_EXCEPTION(HttpStatus.BAD_REQUEST, 170001, "업로드하려는 이미지가 비어있습니다."),
    EXCEPTION_ON_IMAGE_UPLOAD(HttpStatus.BAD_REQUEST, 170002, "이미지 업로드에 실패하였습니다."),
    INVALID_FILE_EXTENSION(HttpStatus.BAD_REQUEST, 170003, "올바르지 않은 파일 형식입니다."),
    IO_EXCEPTION_ON_IMAGE_DELETE(HttpStatus.BAD_REQUEST, 170004, "파일 삭제에 실패하였습니다.")
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
