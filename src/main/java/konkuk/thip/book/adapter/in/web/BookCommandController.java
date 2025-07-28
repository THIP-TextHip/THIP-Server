package konkuk.thip.book.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import konkuk.thip.book.adapter.in.web.request.PostBookIsSavedRequest;
import konkuk.thip.book.adapter.in.web.response.PostBookIsSavedResponse;
import konkuk.thip.book.application.port.in.BookSavedUseCase;
import konkuk.thip.common.dto.BaseResponse;
import konkuk.thip.common.security.annotation.UserId;
import konkuk.thip.common.swagger.annotation.ExceptionDescription;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static konkuk.thip.common.swagger.SwaggerResponseDescription.CHANGE_BOOK_SAVED_STATE;

@Tag(name = "Book Command API", description = "책 상태변경 관련 API")
@Validated
@RestController
@RequiredArgsConstructor
public class BookCommandController {

    private final BookSavedUseCase bookSavedUseCase;

    @Operation(
            summary = "책 저장 상태 변경",
            description = "사용자가 책의 저장 상태를 변경합니다. (true -> 저장, false -> 저장 취소)"
    )
    @ExceptionDescription(CHANGE_BOOK_SAVED_STATE)
    @PostMapping("/books/{isbn}/saved")
    public BaseResponse<PostBookIsSavedResponse> changeSavedBook(
            @Parameter(description = "책의 ISBN 번호 (13자리 숫자)", example = "9781234567890")
            @PathVariable("isbn") @Pattern(regexp = "\\d{13}") final String isbn,
            @RequestBody @Valid final PostBookIsSavedRequest postBookIsSavedRequest,
            @Parameter(hidden = true) @UserId final Long userId
    ) {
        return BaseResponse.ok(PostBookIsSavedResponse.of(bookSavedUseCase.changeSavedBook(isbn,postBookIsSavedRequest.type(),userId)));
    }

}
