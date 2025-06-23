package konkuk.thip.common.dto;

public interface ResponseCode {
    /**
 * Returns the integer code representing this response.
 *
 * @return the response code
 */
int getCode();

    /**
 * Returns the message associated with this response code.
 *
 * @return the response message
 */
String getMessage();

    /**
     * Determines whether this response code represents a successful outcome.
     *
     * @return {@code true} if this instance implements {@code SuccessCode}; {@code false} otherwise.
     */
    default boolean isSuccess() {
        return this instanceof SuccessCode;
    }
}
