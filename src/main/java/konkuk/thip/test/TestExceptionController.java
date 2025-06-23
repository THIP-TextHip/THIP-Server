package konkuk.thip.test;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.common.exception.code.ErrorCode;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/test")
public class TestExceptionController {

    /**
     * Endpoint that always throws an HttpRequestMethodNotSupportedException to simulate a method not allowed error.
     *
     * This endpoint is intended for testing exception handling for unsupported HTTP methods.
     *
     * @throws HttpRequestMethodNotSupportedException always thrown to indicate that only GET is supported.
     */
    @GetMapping("/method-not-allowed")
    public void methodNotAllowed() throws HttpRequestMethodNotSupportedException {
        throw new HttpRequestMethodNotSupportedException("POST", List.of("GET"));
    }

    /**
     * Endpoint that triggers a validation error if the request body fails validation constraints.
     *
     * Expects a JSON request body conforming to {@link DummyRequest}. If the `name` field is blank or missing, a validation exception is raised.
     */
    @GetMapping("/invalid-param")
    public void invalidParam(@Valid @RequestBody DummyRequest request) {
        // 유효성 검사 실패 유도
    }

    /**
     * Endpoint that triggers a type mismatch exception if the 'id' query parameter cannot be parsed as an integer.
     *
     * This method is intended for testing exception handling when a non-integer value is provided for an integer parameter.
     */
    @GetMapping("/type-mismatch")
    public void typeMismatch(@RequestParam Integer id) {
        // id에 문자열 전달 시 예외 발생
    }

    /**
     * Handles GET requests to "/missing-param" and requires the "requiredParam" query parameter.
     *
     * Triggers a missing parameter exception if "requiredParam" is not provided in the request.
     */
    @GetMapping("/missing-param")
    public void missingParam(@RequestParam String requiredParam) {
        // 파라미터 누락 시 예외 발생
    }

    /**
     * Handles GET requests to /business and always throws an EntityNotFoundException with an API_BAD_REQUEST error code.
     *
     * This endpoint is intended for testing business exception handling in the application.
     */
    @GetMapping("/business")
    public void business() {
        throw new EntityNotFoundException(ErrorCode.API_BAD_REQUEST);
    }

    /**
     * Endpoint that always throws an internal server error.
     *
     * This method simulates a server-side failure by throwing an {@link IllegalStateException} when accessed.
     */
    @GetMapping("/runtime")
    public void runtime() {
        throw new IllegalStateException("서버 내부 오류");
    }

    public static class DummyRequest {
        @NotBlank
        public String name;
    }
}
