package konkuk.thip.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;

@Getter
@JsonPropertyOrder({"success", "code", "message"})
public class ErrorResponse {

    @JsonProperty("isSuccess:")
    private final boolean success;

    private final int code;

    private final String message;

    private ErrorResponse(boolean success, int code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }

    private ErrorResponse(ResponseCode response) {
        this(response.isSuccess(), response.getCode(), response.getMessage());
    }

    public static ErrorResponse of(ResponseCode response) {
        return new ErrorResponse(response);
    }
}
