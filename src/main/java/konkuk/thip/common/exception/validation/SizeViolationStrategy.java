package konkuk.thip.common.exception.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.constraints.Size;
import konkuk.thip.common.dto.ErrorResponse;
import konkuk.thip.common.exception.code.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;

import static konkuk.thip.common.exception.code.ErrorCode.API_INVALID_SIZE;


@Component
public class SizeViolationStrategy implements ConstraintViolationStrategy {

    @Override
    public boolean supports(Class<? extends Annotation> annotationType) {
        return Size.class.equals(annotationType);
    }

    @Override
    public ConstraintViolationResult handle(ConstraintViolation<?> violation) {
        HttpStatus status = API_INVALID_SIZE.getHttpStatus();
        ErrorResponse response = ErrorResponse.of(
                ErrorCode.API_INVALID_SIZE,
                violation.getPropertyPath() + "의 길이가 허용 범위를 벗어났습니다."
        );
        return new ConstraintViolationResult(status, response);
    }
}