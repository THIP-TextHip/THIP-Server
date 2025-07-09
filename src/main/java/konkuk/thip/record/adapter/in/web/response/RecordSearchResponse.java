package konkuk.thip.record.adapter.in.web.response;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;

public record RecordSearchResponse(
    List<PostDto> recordList,
    Integer page,
    Integer size,
    Boolean first,
    Boolean last
){

    public static RecordSearchResponse of(List<PostDto> recordList,
                                          Integer page,
                                          Integer size,
                                          Boolean first,
                                          Boolean last) {
        return new RecordSearchResponse(recordList, page, size, first, last);
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = RecordDto.class, name = "RECORD"),
            @JsonSubTypes.Type(value = VoteDto.class, name = "VOTE")
    })
    public sealed interface PostDto permits RecordDto, VoteDto {
        String type();
        String postDate();
        int page();
        Long userId();
        String nickName();
        String profileImageUrl();
        String content();
        int likeCount();
        int commentCount();
        boolean isLiked();
        boolean isWriter();
    }
}
