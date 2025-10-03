package konkuk.thip.common.logging;

public enum LoggingConstant {
    REQUEST_ID("request_id"),
    USER_ID("user_id");

    private final String value;

    LoggingConstant(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
