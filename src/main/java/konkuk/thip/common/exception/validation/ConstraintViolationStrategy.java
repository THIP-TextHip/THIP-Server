package konkuk.thip.common.exception.validation;

import jakarta.validation.ConstraintViolation;

import java.lang.annotation.Annotation;

public interface ConstraintViolationStrategy {
    boolean supports(Class<? extends Annotation> annotationType);
    ConstraintViolationResult handle(ConstraintViolation<?> violation);
}
