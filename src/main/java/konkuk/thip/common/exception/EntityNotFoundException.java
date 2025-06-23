package konkuk.thip.common.exception;

import konkuk.thip.common.exception.code.ErrorCode;

public class EntityNotFoundException extends BusinessException {

    /**
     * Constructs a new EntityNotFoundException with the specified error code.
     *
     * @param errorCode the error code representing the specific entity not found condition
     */
    public EntityNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}
