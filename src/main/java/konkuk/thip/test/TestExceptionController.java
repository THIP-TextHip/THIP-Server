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

    @GetMapping("/method-not-allowed")
    public void methodNotAllowed() throws HttpRequestMethodNotSupportedException {
        throw new HttpRequestMethodNotSupportedException("POST", List.of("GET"));
    }

    @GetMapping("/invalid-param")
    public void invalidParam(@Valid @RequestBody DummyRequest request) {
        // 유효성 검사 실패 유도
    }

    @GetMapping("/type-mismatch")
    public void typeMismatch(@RequestParam Integer id) {
        // id에 문자열 전달 시 예외 발생
    }

    @GetMapping("/missing-param")
    public void missingParam(@RequestParam String requiredParam) {
        // 파라미터 누락 시 예외 발생
    }

    @GetMapping("/business")
    public void business() {
        throw new EntityNotFoundException(ErrorCode.API_BAD_REQUEST);
    }

    @GetMapping("/runtime")
    public void runtime() {
        throw new IllegalStateException("서버 내부 오류");
    }

    public static class DummyRequest {
        @NotBlank
        public String name;
    }
}
