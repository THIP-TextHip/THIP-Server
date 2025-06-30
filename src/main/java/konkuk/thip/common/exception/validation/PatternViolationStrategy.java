package konkuk.thip.common.exception.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.constraints.Pattern;
import konkuk.thip.common.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;

import static konkuk.thip.common.exception.code.ErrorCode.API_INVALID_PATTERN;

@Component
public class PatternViolationStrategy implements ConstraintViolationStrategy {

    @Override
    public boolean supports(Class<? extends Annotation> annotationType) {
        return Pattern.class.equals(annotationType);
    }

    @Override
    public ConstraintViolationResult handle(ConstraintViolation<?> violation) {
        HttpStatus status = API_INVALID_PATTERN.getHttpStatus();
        ErrorResponse response = ErrorResponse.of(
                API_INVALID_PATTERN,
                violation.getPropertyPath() + "의 형식이 올바르지 않습니다."
        );
        return new ConstraintViolationResult(status, response);
    }
}