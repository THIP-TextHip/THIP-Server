package konkuk.thip.common.exception.handler;

import konkuk.thip.common.dto.ErrorResponse;
import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.common.exception.code.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 허용되지 않은 HTTP 메소드로 요청한 경우
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ErrorResponse httpRequestMethodNotSupportedExceptionHandler(HttpRequestMethodNotSupportedException e) {
        log.error("[HttpRequestMethodNotSupportedExceptionHandler] {}", e.getMessage());
        return ErrorResponse.of(ErrorCode.API_METHOD_NOT_ALLOWED);
    }

    // 요청 파라미터가 유효하지 않은 경우
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResponse methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        log.error("[MethodArgumentNotValidExceptionHandler] {}", e.getMessage());
        return ErrorResponse.of(ErrorCode.API_INVALID_PARAM);
    }

    // 요청 파라미터의 타입이 맞지 않는 경우
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ErrorResponse methodArgumentTypeMismatchExceptionHandler(MethodArgumentTypeMismatchException e) {
        log.error("[MethodArgumentTypeMismatchExceptionHandler] {}", e.getMessage());
        return ErrorResponse.of(ErrorCode.API_INVALID_TYPE);
    }

    // 요청 파라미터가 누락된 경우
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ErrorResponse missingServletRequestParameterExceptionHandler(MissingServletRequestParameterException e) {
        log.error("[MissingServletRequestParameterExceptionHandler] {}", e.getMessage());
        return ErrorResponse.of(ErrorCode.API_MISSING_PARAM);
    }

    // 비즈니스 로직에서 발생한 예외 처리
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BusinessException.class)
    public ErrorResponse businessExceptionHandler(BusinessException e) {
        log.error("[BusinessExceptionHandler] {}", e.getMessage());
        return ErrorResponse.of(e.getErrorCode());
    }

    // 서버 내부 오류 예외 처리
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({RuntimeException.class, IllegalStateException.class})
    public ErrorResponse runtimeExceptionHandler(Exception e) {
        log.error("[RuntimeExceptionHandler] {}", e.getMessage());
        return ErrorResponse.of(ErrorCode.API_SERVER_ERROR);
    }

}
