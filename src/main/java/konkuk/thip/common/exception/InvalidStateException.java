package konkuk.thip.common.exception;

import konkuk.thip.common.exception.code.ErrorCode;
import lombok.Getter;

@Getter
public class InvalidStateException extends BusinessException {
    public InvalidStateException(ErrorCode errorCode) {
        super(errorCode);
    }

    public InvalidStateException(ErrorCode errorCode, Exception e) {
        super(errorCode, e);
    }
}
