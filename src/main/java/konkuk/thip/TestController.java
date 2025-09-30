package konkuk.thip;

import konkuk.thip.common.security.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@ConditionalOnProperty(name = "thip.test-api.enabled", havingValue = "true")
public class TestController {

    private final JwtUtil jwtUtil;

    @GetMapping("/api/test/token/access")
    public String generateAccessToken(@RequestParam Long userId) {
        return jwtUtil.createAccessToken(userId);
    }

    @GetMapping("/api/test/error")
    public String throwError() {
        throw new RuntimeException("테스트 500 에러");
    }
}
