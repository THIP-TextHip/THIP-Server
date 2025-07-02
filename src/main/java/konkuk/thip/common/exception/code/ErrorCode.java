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

    /* 60000부터 비즈니스 예외 */
    /**
     * 60000 : alias error
     */
    ALIAS_NOT_FOUND(HttpStatus.NOT_FOUND, 60001, "존재하지 않는 ALIAS 입니다."),


    /**
     * 70000 : user error
     */
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, 70000, "존재하지 않는 USER 입니다."),

    /**
     * 80000 : book error
     */
    BOOK_KEYWORD_ENCODING_FAILED(HttpStatus.BAD_REQUEST, 80000, "검색어 인코딩에 실패했습니다."),
    BOOK_NAVER_API_REQUEST_ERROR(HttpStatus.BAD_REQUEST, 80001,"네이버 API 요청에 실패하였습니다."),
    BOOK_NAVER_API_PARSING_ERROR(HttpStatus.BAD_REQUEST, 80002,"네이버 API 응답 파싱에 실패하였습니다."),
    BOOK_NAVER_API_URL_ERROR(HttpStatus.BAD_REQUEST, 80003,"네이버 API URL이 잘못되었습니다."),
    BOOK_NAVER_API_URL_HTTP_CONNECT_FAILED(HttpStatus.BAD_REQUEST, 80004,"네이버 API 요청 중, HTTP 연결에 실패하였습니다."),
    BOOK_NAVER_API_RESPONSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 80005,"네이버 API 응답에 실패하였습니다."),
    BOOK_SEARCH_PAGE_OUT_OF_RANGE(HttpStatus.BAD_REQUEST, 80006,"검색어 페이지가 범위를 벗어났습니다."),
    BOOK_KEYWORD_REQUIRED(HttpStatus.BAD_REQUEST, 80007, "검색어는 필수 입력값입니다."),
    BOOK_PAGE_NUMBER_INVALID(HttpStatus.BAD_REQUEST, 80008, "페이지 번호는 1 이상의 값이어야 합니다."),
    BOOK_ISBN_NOT_FOUND(HttpStatus.BAD_REQUEST, 80009, "ISBN으로 검색한 결과가 존재하지 않습니다."),
    BOOK_NOT_FOUND(HttpStatus.BAD_REQUEST, 80010, "존재하지 않는 BOOK 입니다.");






    private final HttpStatus httpStatus;
    private final int code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, int code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
