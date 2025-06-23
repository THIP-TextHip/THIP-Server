package konkuk.thip.common.dto;

import lombok.Getter;

@Getter
public enum SuccessCode implements ResponseCode {
    API_SUCCESS(20000, "요청에 성공했습니다."),
    ;

    private final int code;
    private final String message;

    /**
     * Constructs a SuccessCode enum constant with the specified code and message.
     *
     * @param code the integer code representing the success status
     * @param message the message describing the success status
     */
    SuccessCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
