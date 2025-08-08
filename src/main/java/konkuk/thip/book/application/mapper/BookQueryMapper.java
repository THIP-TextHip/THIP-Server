package konkuk.thip.book.application.mapper;

import konkuk.thip.book.adapter.in.web.response.BookRecruitingRoomsResponse;
import konkuk.thip.book.application.port.in.dto.BookInfo;
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
    BookRecruitingRoomsResponse.RecruitingRoomDto toRecruitingRoomDto(RoomQueryDto dto);

    List<BookRecruitingRoomsResponse.RecruitingRoomDto> toRecruitingRoomDtoList(List<RoomQueryDto> roomDtos);

    @Mapping(target = "bookId", source = "book.id")
    @Mapping(target = "bookTitle", source = "book.title")
    @Mapping(target = "authorName", source = "book.authorName")
    @Mapping(target = "publisher", source = "book.publisher")
    @Mapping(target = "bookImageUrl", source = "book.imageUrl")
    @Mapping(target = "isbn", source = "book.isbn")
    BookInfo toBookInfo(Book book);

    List<BookInfo> toBookInfoList(List<Book> books);
}
