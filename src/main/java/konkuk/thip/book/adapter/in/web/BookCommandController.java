package konkuk.thip.book.adapter.in.web;

import jakarta.validation.constraints.Pattern;
import konkuk.thip.book.adapter.in.web.request.PostBookIsSavedRequest;
import konkuk.thip.book.adapter.in.web.response.PostBookIsSavedResponse;
import konkuk.thip.book.application.port.in.BookSavedUseCase;
import konkuk.thip.common.dto.BaseResponse;
import konkuk.thip.common.security.annotation.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
public class BookCommandController {

    private final BookSavedUseCase bookSavedUseCase;

    //책 저장 상태 변경
    @PostMapping("/books/{isbn}/saved")
    public BaseResponse<PostBookIsSavedResponse> isSavedBook(@PathVariable("isbn")
                                                                         @Pattern(regexp = "\\d{13}") final String isbn,
                                                             @RequestBody final PostBookIsSavedRequest postBookIsSavedRequest,
                                                             @UserId final Long userId) {
        return BaseResponse.ok(PostBookIsSavedResponse.of(bookSavedUseCase.isSavedBook(isbn,postBookIsSavedRequest,userId)));
    }

}
