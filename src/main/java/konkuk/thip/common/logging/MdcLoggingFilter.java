package konkuk.thip.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

import static konkuk.thip.common.logging.LoggingConstant.REQUEST_ID;

@Component
public class MdcLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String rawRequestId = request.getHeader("X-Request-ID");
            String requestId = (rawRequestId == null || rawRequestId.trim().isEmpty())
                    ? UUID.randomUUID().toString()
                    : rawRequestId;

            MDC.put(REQUEST_ID.getValue(), requestId);
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}