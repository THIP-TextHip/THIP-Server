package konkuk.thip.common.exception;

import konkuk.thip.common.exception.code.ErrorCode;

public class InternalServerException extends RuntimeException {

    private final ErrorCode errorCode;

    public InternalServerException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    public InternalServerException(ErrorCode errorCode, Exception e) {
        super(errorCode.getMessage(), e);
        this.errorCode = errorCode;
    }
}
