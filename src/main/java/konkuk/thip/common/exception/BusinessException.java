package konkuk.thip.common.exception;

import konkuk.thip.common.exception.code.ErrorCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;

    /**
     * Constructs a new BusinessException with the specified error code.
     *
     * @param errorCode the error code representing the specific business error
     */
    public BusinessException(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * Constructs a new BusinessException with the specified error code and cause.
     *
     * @param errorCode the error code representing the specific business error
     * @param e the underlying exception that caused this exception
     */
    public BusinessException(ErrorCode errorCode, Exception e) {
        super(e);
        this.errorCode = errorCode;
    }
}
