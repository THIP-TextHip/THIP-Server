package konkuk.thip.common.exception;

import konkuk.thip.common.exception.code.ErrorCode;
import lombok.Getter;

@Getter
public class InvalidStateException extends BusinessException {
    /**
     * Constructs an InvalidStateException with the specified error code.
     *
     * @param errorCode the error code representing the invalid state
     */
    public InvalidStateException(ErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * Constructs an InvalidStateException with the specified error code and underlying exception.
     *
     * @param errorCode the error code representing the specific invalid state
     * @param e the underlying exception that caused this exception
     */
    public InvalidStateException(ErrorCode errorCode, Exception e) {
        super(errorCode, e);
    }
}
