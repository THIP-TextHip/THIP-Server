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

    /**
     * Handles requests made with unsupported HTTP methods and returns a standardized error response.
     *
     * @return an ErrorResponse indicating the HTTP method is not allowed
     */
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ErrorResponse httpRequestMethodNotSupportedExceptionHandler(HttpRequestMethodNotSupportedException e) {
        log.error("[HttpRequestMethodNotSupportedExceptionHandler] {}", e.getMessage());
        return ErrorResponse.of(ErrorCode.API_METHOD_NOT_ALLOWED);
    }

    /**
     * Handles invalid request parameters by returning a standardized error response.
     *
     * This method is invoked when a request fails validation due to invalid parameters, responding with HTTP 400 (Bad Request) and an error code indicating invalid input.
     *
     * @return an ErrorResponse representing the invalid parameter error
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResponse methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        log.error("[MethodArgumentNotValidExceptionHandler] {}", e.getMessage());
        return ErrorResponse.of(ErrorCode.API_INVALID_PARAM);
    }

    /**
     * Handles cases where a request parameter cannot be converted to the expected type.
     *
     * Returns an error response with a code indicating an invalid parameter type and a 400 Bad Request status.
     *
     * @return an ErrorResponse indicating an invalid parameter type
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ErrorResponse methodArgumentTypeMismatchExceptionHandler(MethodArgumentTypeMismatchException e) {
        log.error("[MethodArgumentTypeMismatchExceptionHandler] {}", e.getMessage());
        return ErrorResponse.of(ErrorCode.API_INVALID_TYPE);
    }

    /**
     * Handles cases where a required request parameter is missing and returns a standardized error response.
     *
     * @return an ErrorResponse indicating a missing parameter, with HTTP status 400 (Bad Request)
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ErrorResponse missingServletRequestParameterExceptionHandler(MissingServletRequestParameterException e) {
        log.error("[MissingServletRequestParameterExceptionHandler] {}", e.getMessage());
        return ErrorResponse.of(ErrorCode.API_MISSING_PARAM);
    }

    /**
     * Handles BusinessException thrown from business logic and returns a standardized error response.
     *
     * @param e the BusinessException containing the specific error code
     * @return an ErrorResponse constructed from the exception's error code, with HTTP status 400 (Bad Request)
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BusinessException.class)
    public ErrorResponse businessExceptionHandler(BusinessException e) {
        log.error("[BusinessExceptionHandler] {}", e.getMessage());
        return ErrorResponse.of(e.getErrorCode());
    }

    /**
     * Handles unexpected server errors and returns a standardized error response with HTTP status 500.
     *
     * @return an ErrorResponse indicating an internal server error
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({RuntimeException.class, IllegalStateException.class})
    public ErrorResponse runtimeExceptionHandler(Exception e) {
        log.error("[RuntimeExceptionHandler] {}", e.getMessage());
        return ErrorResponse.of(ErrorCode.API_SERVER_ERROR);
    }

}
