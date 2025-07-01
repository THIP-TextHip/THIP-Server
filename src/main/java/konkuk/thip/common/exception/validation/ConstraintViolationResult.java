package konkuk.thip.common.exception.validation;

import konkuk.thip.common.dto.ErrorResponse;
import org.springframework.http.HttpStatus;

public record ConstraintViolationResult(
        HttpStatus httpStatus,
        ErrorResponse errorResponse) {
}