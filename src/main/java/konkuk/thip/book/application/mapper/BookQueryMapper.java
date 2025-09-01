package konkuk.thip.book.application.mapper;

import konkuk.thip.book.adapter.in.web.response.BookRecruitingRoomsResponse;
import konkuk.thip.book.adapter.in.web.response.BookSelectableListResponse;
import konkuk.thip.book.adapter.in.web.response.BookShowSavedListResponse;
import konkuk.thip.book.application.port.in.dto.BookPinResult;
import konkuk.thip.book.application.port.out.dto.BookQueryDto;
import konkuk.thip.book.domain.Book;
import konkuk.thip.common.util.DateUtil;
import konkuk.thip.room.application.port.out.dto.RoomQueryDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        imports = DateUtil.class,
        unmappedTargetPolicy = ReportingPolicy.IGNORE       // 명시적으로 매핑하지 않은 필드를 무시하도록 설정
)
public interface BookQueryMapper {

    @Mapping(
            target = "deadlineEndDate",
            expression = "java(DateUtil.formatAfterTime(dto.endDate()))"
    )
    BookRecruitingRoomsResponse.BookRecruitingRoomDto toRecruitingRoomDto(RoomQueryDto dto);

    List<BookRecruitingRoomsResponse.BookRecruitingRoomDto> toRecruitingRoomDtoList(List<RoomQueryDto> roomDtos);

    @Mapping(target = "bookTitle", source = "book.title")
    @Mapping(target = "authorName", source = "book.authorName")
    @Mapping(target = "bookImageUrl", source = "book.imageUrl")
    @Mapping(target = "isbn", source = "book.isbn")
    BookPinResult toBookPinResult(Book book);

    @Mapping(target = "isSaved", constant = "true")
    BookShowSavedListResponse.BookShowSavedDto toBookShowSavedDto(BookQueryDto dto);

    List<BookShowSavedListResponse.BookShowSavedDto> toBookShowSavedListResponse(List<BookQueryDto> dtos);

    BookSelectableListResponse.BookSelectableDto  toBookSelectableDto(BookQueryDto dto);

    List<BookSelectableListResponse.BookSelectableDto> toBookSelectableListResponse(List<BookQueryDto> contents);
}
