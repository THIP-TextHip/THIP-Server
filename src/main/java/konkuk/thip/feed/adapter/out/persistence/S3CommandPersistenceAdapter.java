package konkuk.thip.feed.adapter.out.persistence;

import konkuk.thip.feed.adapter.out.s3.S3Service;
import konkuk.thip.feed.application.port.out.S3CommandPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class S3CommandPersistenceAdapter implements S3CommandPort {

    private final S3Service s3Service;

    @Override
    public List<String> uploadImages(List<MultipartFile> images) {
        return images.stream()
                .map(s3Service::uploadUserImageAndGetUrl)
                .toList();
    }

    @Override
    public void deleteImages(List<String> imageUrls) {
        imageUrls.forEach(s3Service::deleteImageFromS3);
    }
}
