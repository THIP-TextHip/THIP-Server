package konkuk.thip.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;

@Getter
@JsonPropertyOrder({"success", "code", "message", "data"})
public class BaseResponse<T> {

    @JsonProperty("isSuccess")
    private final boolean success;

    private final int code;

    private final String message;

    private final T data;

    private BaseResponse(boolean success, int code, String message, T data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    private BaseResponse(ResponseCode response, T data) {
        this(response.isSuccess(), response.getCode(), response.getMessage(), data);
    }

    public static <T> BaseResponse<T> ok(T data) {
        return new BaseResponse<>(SuccessCode.API_SUCCESS, data);
    }

}