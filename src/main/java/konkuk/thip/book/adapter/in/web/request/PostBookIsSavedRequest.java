package konkuk.thip.book.adapter.in.web.request;

import jakarta.validation.constraints.NotNull;


public record PostBookIsSavedRequest(
        @NotNull(message = "type은 필수입니다.")
        boolean type
) {
}
