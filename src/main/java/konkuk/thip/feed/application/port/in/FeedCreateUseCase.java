package konkuk.thip.feed.application.port.in;

import konkuk.thip.feed.application.port.in.dto.FeedCreateCommand;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FeedCreateUseCase {
    Long createFeed(FeedCreateCommand command);
}
