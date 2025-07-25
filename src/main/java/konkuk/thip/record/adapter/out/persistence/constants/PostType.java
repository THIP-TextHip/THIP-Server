package konkuk.thip.record.adapter.out.persistence.constants;

import lombok.Getter;

@Getter
public enum PostType {
    RECORD("RECORD"),
    VOTE("VOTE"),
    UNKNOWN("UNKNOWN")
    ;

    private final String type;

    PostType(String type) {
        this.type = type;
    }
}
