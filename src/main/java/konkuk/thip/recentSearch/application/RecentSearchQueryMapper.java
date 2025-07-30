package konkuk.thip.recentSearch.application;

import konkuk.thip.recentSearch.adapter.in.web.response.RecentSearchGetResponse;
import konkuk.thip.recentSearch.domain.RecentSearch;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RecentSearchQueryMapper {

    @Mapping(source = "id", target = "recentSearchId")
    RecentSearchGetResponse.RecentSearchDto toDto(RecentSearch recentSearch);

    List<RecentSearchGetResponse.RecentSearchDto> toResponseList(List<RecentSearch> recentSearchQueryDtos);
}
