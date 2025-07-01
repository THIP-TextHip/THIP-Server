package konkuk.thip.common.exception;

import konkuk.thip.common.exception.code.ErrorCode;

public class ExternalApiException extends RuntimeException {

    private final ErrorCode errorCode;

    public ExternalApiException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    public ExternalApiException(ErrorCode errorCode, Exception e) {
        super(errorCode.getMessage(), e);
        this.errorCode = errorCode;
    }
}
