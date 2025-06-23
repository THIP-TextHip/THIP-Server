package konkuk.thip.common.exception.code;

import konkuk.thip.common.dto.ResponseCode;
import lombok.Getter;

@Getter
public enum ErrorCode implements ResponseCode {

    API_METHOD_NOT_ALLOWED(40500, "허용되지 않는 HTTP 메소드입니다."),
    API_SERVER_ERROR(50000, "서버 내부 오류입니다."),

    API_BAD_REQUEST(40002, "잘못된 요청입니다."),
    API_MISSING_PARAM(40001, "필수 파라미터가 없습니다."),
    API_INVALID_PARAM(40002, "파라미터 값 중 유효하지 않은 값이 있습니다."),
    API_INVALID_TYPE(40003, "파라미터 타입이 잘못되었습니다."),

    /* 60000부터 비즈니스 예외 */
    ;

    private final int code;
    private final String message;

    /**
     * Constructs an ErrorCode enum constant with the specified numeric code and descriptive message.
     *
     * @param code the unique integer representing the error code
     * @param message the description associated with the error code
     */
    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
