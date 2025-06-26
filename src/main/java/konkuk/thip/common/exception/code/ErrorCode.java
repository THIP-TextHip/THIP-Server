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

    /* 60000부터 비즈니스 예외 */
    /**
     * 60000 : alias error
     */
    ALIAS_NOT_FOUND(HttpStatus.NOT_FOUND, 60001, "존재하지 않는 ALIAS 입니다.");


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
