package konkuk.thip.common.exception;

import konkuk.thip.common.exception.code.ErrorCode;

public class FirebaseException extends RuntimeException {

    private final ErrorCode errorCode;

    public FirebaseException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    public FirebaseException(ErrorCode errorCode, Exception e) {
        super(errorCode.getMessage(), e);
        this.errorCode = errorCode;
    }
}
