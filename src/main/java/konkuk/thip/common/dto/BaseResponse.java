package konkuk.thip.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import static konkuk.thip.common.logging.LoggingConstant.REQUEST_ID;

@Slf4j
@Getter
@JsonPropertyOrder({"success", "code", "message", "requestId", "data"})
public class BaseResponse<T> {

    @JsonProperty("isSuccess")
    private final boolean success;

    private final int code;

    private final String message;

    private final String requestId;

    private final T data;

    private BaseResponse(boolean success, int code, String message, String requestId, T data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.requestId = requestId;
        this.data = data;
    }

    private BaseResponse(ResponseCode response, T data) {
        this(response.isSuccess(), response.getCode(), response.getMessage(), MDC.get(REQUEST_ID.getValue()), data);
    }

    public static <T> BaseResponse<T> ok(T data) {
        return new BaseResponse<>(SuccessCode.API_SUCCESS, data);
    }

}