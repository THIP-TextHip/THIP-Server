package konkuk.thip.common.dto;

public interface ResponseCode {
    int getCode();

    String getMessage();

    default boolean isSuccess() {
        return this instanceof SuccessCode;
    }
}
