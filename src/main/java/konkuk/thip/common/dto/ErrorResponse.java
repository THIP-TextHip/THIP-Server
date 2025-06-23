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

    /**
     * Constructs an ErrorResponse with the specified success status, error code, and message.
     *
     * @param success whether the operation was successful
     * @param code    numeric code representing the error type
     * @param message descriptive message about the error
     */
    private ErrorResponse(boolean success, int code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }

    /**
     * Initializes an ErrorResponse using the properties of the given ResponseCode.
     *
     * @param response the ResponseCode containing the success status, error code, and message
     */
    private ErrorResponse(ResponseCode response) {
        this(response.isSuccess(), response.getCode(), response.getMessage());
    }

    /**
     * Creates an ErrorResponse instance based on the provided ResponseCode.
     *
     * @param response the ResponseCode containing error details
     * @return an ErrorResponse representing the given ResponseCode
     */
    public static ErrorResponse of(ResponseCode response) {
        return new ErrorResponse(response);
    }
}
