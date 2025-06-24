package konkuk.thip.common.exception.handler;

import konkuk.thip.common.dto.ErrorResponse;
import konkuk.thip.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import static konkuk.thip.common.exception.code.ErrorCode.*;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

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

    // 비즈니스 로직에서 발생한 예외 처리
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> businessExceptionHandler(BusinessException e) {
        log.error("[BusinessExceptionHandler] {}", e.getMessage());
        return ResponseEntity
                .status(e.getErrorCode().getHttpStatus())
                .body(ErrorResponse.of(e.getErrorCode()));
    }

    // 서버 내부 오류 예외 처리
    @ExceptionHandler({RuntimeException.class, IllegalStateException.class})
    public ResponseEntity<ErrorResponse> runtimeExceptionHandler(Exception e) {
        log.error("[RuntimeExceptionHandler] {}", e.getMessage());
        return ResponseEntity
                .status(API_SERVER_ERROR.getHttpStatus())
                .body(ErrorResponse.of(API_SERVER_ERROR));
    }

}
