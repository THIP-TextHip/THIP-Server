package konkuk.thip.common.exception.handler;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import konkuk.thip.common.discord.DiscordClient;
import konkuk.thip.common.dto.ErrorResponse;
import konkuk.thip.common.exception.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Optional;

import static konkuk.thip.common.exception.code.ErrorCode.*;
import static konkuk.thip.common.logging.LoggingConstant.REQUEST_ID;
import static konkuk.thip.common.logging.LoggingConstant.USER_ID;
import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final DiscordClient discordClient;

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

        // 1) cause 에 포함된 상세 메시지를 파싱, 없다면 빈 문자열로 설정
        String detail = Optional.ofNullable(e.getCause())
                .map(Throwable::getMessage)
                .orElse("");

        return ResponseEntity
                .status(e.getErrorCode().getHttpStatus())
                .body(ErrorResponse.of(e.getErrorCode(), detail));
    }

    // 서버 내부 오류 예외 (500) 처리
    @ExceptionHandler({RuntimeException.class, IllegalStateException.class,
            FirebaseException.class, InternalServerException.class, ExternalApiException.class})
    public ResponseEntity<ErrorResponse> handleServerErrors(Exception e) {
        log.error("[ServerErrorHandler] {}", e.getMessage(), e);

        String exceptionClassName = e.getClass().getSimpleName(); // 예외 클래스명
        String combinedMessage = "[" + exceptionClassName + "] " + e.getMessage(); // 메시지에 예외 클래스명 포함
        String stackTrace = getStackTrace(e);

        // 스택트레이스 요약: 두,세번째 줄 + 마지막줄
        String[] lines = stackTrace.split("\n");
        String stackSummary;
        if (lines.length <= 3) {
            stackSummary = stackTrace; // 짧으면 다 보여줌
        } else {
            stackSummary = lines[1] + "\n" + lines[2] + "\n" + lines[lines.length - 1];
        }

        // MDC에서 requestId, userId 추출
        String requestId = MDC.get(REQUEST_ID.getValue());
        String userId = MDC.get(USER_ID.getValue());

        // Discord 웹훅 전송
        discordClient.sendErrorMessage(combinedMessage, stackSummary, requestId, userId);

        return ResponseEntity
                .status(API_SERVER_ERROR.getHttpStatus())
                .body(ErrorResponse.of(API_SERVER_ERROR));
    }

    // @validation 예외처리
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> constraintViolationExceptionHandler(ConstraintViolationException e) {
        log.error("[ConstraintViolationExceptionHandler] {}", e.getMessage());
        // 첫 번째 위반만 꺼내서
        ConstraintViolation<?> violation = e.getConstraintViolations().stream().findFirst().orElse(null);

        // 기본 메시지 또는 제약조건 메시지 사용
        String errorMessage = Optional.ofNullable(violation)
                .map(v -> v.getMessage())
                .orElse("유효성 검사에 실패했습니다.");

        // API_INVALID_PARAM 코드를 공통으로 사용
        return ResponseEntity
                .status(API_INVALID_PARAM.getHttpStatus())
                .body(ErrorResponse.of(API_INVALID_PARAM, errorMessage));
    }

}
