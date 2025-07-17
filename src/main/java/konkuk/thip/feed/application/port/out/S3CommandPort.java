package konkuk.thip.feed.application.port.out;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface S3CommandPort {
    List<String> uploadImages(List<MultipartFile> images);
    void deleteImages(List<String> imageUrls);
}
