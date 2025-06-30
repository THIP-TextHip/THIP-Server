package konkuk.thip.common.exception.handler;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import konkuk.thip.common.dto.ErrorResponse;
import konkuk.thip.common.exception.AuthException;
import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.common.exception.validation.ConstraintViolationResult;
import konkuk.thip.common.exception.validation.ConstraintViolationStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.lang.annotation.Annotation;
import java.util.List;

import static konkuk.thip.common.exception.code.ErrorCode.*;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final List<ConstraintViolationStrategy> violationStrategies;

    // 요청한 API가 없는 경우
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> noHandlerExceptionHandler(NoHandlerFoundException e) {
        return ResponseEntity
                .status(API_NOT_FOUND.getHttpStatus())
                .body(ErrorResponse.of(API_NOT_FOUND));
    }

    // 허용되지 않은 HTTP 메소드로 요청한 경우
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> httpRequestMethodNotSupportedExceptionHandler(HttpRequestMethodNotSupportedException e) {
        log.error("[HttpRequestMethodNotSupportedExceptionHandler] {}", e.getMessage());
        return ResponseEntity
                .status(API_METHOD_NOT_ALLOWED.getHttpStatus())
                .body(ErrorResponse.of(API_METHOD_NOT_ALLOWED));
    }

    // 요청 파라미터가 유효하지 않은 경우
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        log.error("[MethodArgumentNotValidExceptionHandler] {}", e.getMessage());
        // 첫 번째 유효성 검사 실패 메시지만 가져오기
        String errorMessage = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Validation failed");

        return ResponseEntity
                .status(API_INVALID_PARAM.getHttpStatus())
                .body(ErrorResponse.of(API_INVALID_PARAM, errorMessage));
    }

    // 요청 파라미터의 타입이 맞지 않는 경우
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> methodArgumentTypeMismatchExceptionHandler(MethodArgumentTypeMismatchException e) {
        log.error("[MethodArgumentTypeMismatchExceptionHandler] {}", e.getMessage());

        return ResponseEntity
                .status(API_INVALID_TYPE.getHttpStatus())
                .body(ErrorResponse.of(API_INVALID_TYPE, e.getName() + "는 " + e.getRequiredType() + " 타입이어야 합니다."));
    }

    // 요청 파라미터가 누락된 경우
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> missingServletRequestParameterExceptionHandler(MissingServletRequestParameterException e) {
        log.error("[MissingServletRequestParameterExceptionHandler] {}", e.getMessage());
        return ResponseEntity
                .status(API_MISSING_PARAM.getHttpStatus())
                .body(ErrorResponse.of(API_MISSING_PARAM, e.getParameterName() + "를 추가해서 요청해주세요."));
    }

    // 인증, 인가 권한 관련 예외 처리
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorResponse> authExceptionHandler(AuthException e) {
        log.error("[AuthExceptionHandler] {}", e.getMessage());
        return ResponseEntity
                .status(e.getErrorCode().getHttpStatus())
                .body(ErrorResponse.of(e.getErrorCode()));
    }

    // 비즈니스 로직에서 발생한 예외 처리
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> businessExceptionHandler(BusinessException e) {
        log.error("[BusinessExceptionHandler] {}", e.getMessage());
        return ResponseEntity
                .status(e.getErrorCode().getHttpStatus())
                .body(ErrorResponse.of(e.getErrorCode()));
    }

    // 서버 내부 오류 예외 처리
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> runtimeExceptionHandler(RuntimeException e) {
        log.error("[RuntimeExceptionHandler] {}", e.getMessage());
        return ResponseEntity
                .status(API_SERVER_ERROR.getHttpStatus())
                .body(ErrorResponse.of(API_SERVER_ERROR));
    }

    // IllegalStateException 예외 처리
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> illegalStateExceptionHandler(IllegalStateException e) {
        log.error("[IllegalStateExceptionHandler] {}", e.getMessage());
        return ResponseEntity
                .status(API_SERVER_ERROR.getHttpStatus())
                .body(ErrorResponse.of(API_SERVER_ERROR));
    }

    // @validation 예외처리
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> constraintViolationExceptionHandler(ConstraintViolationException e) {
        log.error("[ConstraintViolationExceptionHandler] {}", e.getMessage());
        // 첫 번째 위반을 꺼내서
        ConstraintViolation<?> violation = e.getConstraintViolations().stream().findFirst().orElse(null);
        if (violation != null) {
            Class<? extends Annotation> annotationType =
                    violation.getConstraintDescriptor().getAnnotation().annotationType();

            // 등록된 전략 중 supports()를 만족하는 첫 번째 전략에 위임
            for (ConstraintViolationStrategy strategy : violationStrategies) {
                if (strategy.supports(annotationType)) {
                    ConstraintViolationResult result = strategy.handle(violation);
                    return ResponseEntity.status(result.httpStatus()).body(result.errorResponse());
                }
            }

            // 지원하지 않는 제약인 경우 기본 처리
            return ResponseEntity
                    .status(API_REQUEST_INVALID.getHttpStatus())
                    .body(ErrorResponse.of(API_REQUEST_INVALID, violation.getMessage()));
        }

        // violation 자체가 없으면 최종 폴백
        return ResponseEntity
                .status(API_REQUEST_INVALID.getHttpStatus())
                .body(ErrorResponse.of(API_REQUEST_INVALID, "잘못된 요청입니다."));
    }



}
