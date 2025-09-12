package konkuk.thip.common.exception;

import konkuk.thip.common.exception.code.ErrorCode;

public class FirebaseException extends BusinessException {
    public FirebaseException(ErrorCode errorCode) {
        super(errorCode);
    }

    public FirebaseException(ErrorCode errorCode, Exception e) {
        super(errorCode, e);
    }

    public FirebaseException(Exception e) {
        super(ErrorCode.FIREBASE_SEND_ERROR, e);
    }

    public FirebaseException() {
        super(ErrorCode.FIREBASE_SEND_ERROR);
    }
}
