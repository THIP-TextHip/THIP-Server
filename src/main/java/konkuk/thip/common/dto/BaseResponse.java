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

    /**
     * Constructs a BaseResponse with the specified success flag, response code, message, and data payload.
     *
     * @param success whether the response indicates success
     * @param code the response status code
     * @param message a descriptive message about the response
     * @param data the payload of the response
     */
    private BaseResponse(boolean success, int code, String message, T data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * Constructs a BaseResponse using the properties of the given ResponseCode and the provided data payload.
     *
     * @param response the ResponseCode containing success status, code, and message
     * @param data the response payload
     */
    private BaseResponse(ResponseCode response, T data) {
        this(response.isSuccess(), response.getCode(), response.getMessage(), data);
    }

    /**
     * Creates a successful API response containing the provided data.
     *
     * @param data the payload to include in the response
     * @return a BaseResponse instance representing a successful operation with the given data
     */
    public static <T> BaseResponse<T> ok(T data) {
        return new BaseResponse<>(SuccessCode.API_SUCCESS, data);
    }

}