package konkuk.thip.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import org.slf4j.MDC;

import static konkuk.thip.common.logging.LoggingConstant.REQUEST_ID;

@Getter
@JsonPropertyOrder({"success", "code", "message", "requestId"})
public class ErrorResponse {

    @JsonProperty("isSuccess")
    private final boolean success;

    private final int code;

    private final String message;

    private final String requestId;

    private ErrorResponse(boolean success, int code, String message, String requestId) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.requestId = requestId;
    }

    private ErrorResponse(ResponseCode response) {
        this(response.isSuccess(), response.getCode(), response.getMessage(), MDC.get(REQUEST_ID.getValue()));
    }

    public static ErrorResponse of(ResponseCode response) {
        return new ErrorResponse(response);
    }

    public static ErrorResponse of(ResponseCode response, String message) {
        StringBuilder sb =  new StringBuilder();
        sb.append(response.getMessage()).append(" ").append(message);
        return new ErrorResponse(response.isSuccess(), response.getCode(), sb.toString(), MDC.get(REQUEST_ID.getValue()));
    }
}
