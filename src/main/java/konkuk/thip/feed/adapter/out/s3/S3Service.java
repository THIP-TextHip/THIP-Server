package konkuk.thip.feed.adapter.out.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.util.IOUtils;
import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.common.exception.InvalidStateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static konkuk.thip.common.exception.code.ErrorCode.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 amazonS3; // AWS S3 클라이언트

    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "gif");

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * 유저가 업로드한 이미지 파일을 받아 S3에 저장한 후, public URL을 반환합니다.
     * @param image 업로드할 이미지 파일
     * @return S3의 public 이미지 URL
     */
    public String uploadUserImageAndGetUrl(MultipartFile image) {
        //입력받은 이미지 파일이 빈 파일인지 검증
        if(image.isEmpty() || Objects.isNull(image.getOriginalFilename())){
            throw new BusinessException(EMPTY_FILE_EXCEPTION);
        }
        return uploadAndReturnUrl(image);
    }

    /**
     * 이미지 파일 확장자 검증 후 S3 업로드 (실제 업로드는 내부 메서드에서 처리)
     */
    private String uploadAndReturnUrl(MultipartFile image) {
        this.validateImageFileExtension(image.getOriginalFilename());
        try {
            return uploadImageToS3(image);
        } catch (IOException e) {
            throw new BusinessException(EXCEPTION_ON_IMAGE_UPLOAD);
        }
    }

    /**
     * 이미지 파일 확장자가 jpg, jpeg, png, gif 중 하나인지 검증
     */
    private void validateImageFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            throw new InvalidStateException(INVALID_FILE_EXTENSION);
        }

        String extension = filename.substring(lastDotIndex + 1).toLowerCase();

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new InvalidStateException(INVALID_FILE_EXTENSION);
        }
    }

    /**
     * 실제 이미지를 S3에 업로드하고 public URL을 반환
     */
    private String uploadImageToS3(MultipartFile image) throws IOException {
        String originalFilename = image.getOriginalFilename(); //원본 파일 명
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1); //확장자 명

        // UUID + 원본 파일명을 합쳐 S3에 저장 (중복 방지)
        String s3FileName = UUID.randomUUID().toString().substring(0, 10) + originalFilename;

        // 이미지 파일을 바이트 배열로 변환
        InputStream is = image.getInputStream();
        byte[] bytes = IOUtils.toByteArray(is);

        // S3 메타데이터 생성(컨텐츠타입, 길이 등)
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("image/" + extension);
        metadata.setContentLength(bytes.length);

        // 바이트 배열로부터 InputStream 생성
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

        try {
            // S3에 업로드
            amazonS3.putObject(bucket, s3FileName, byteArrayInputStream, metadata);

        } catch (Exception e) {
            throw new BusinessException(EXCEPTION_ON_IMAGE_UPLOAD);
        } finally {
            byteArrayInputStream.close();
            is.close();
        }

        // 업로드 성공시에 S3 파일의 public URL 반환
        return amazonS3.getUrl(bucket, s3FileName).toString();
    }


    /**
     * S3에 저장된 이미지를 삭제
     * @param imageAddress 이미지 public URL (S3 경로)
     */
    @Async
    public void deleteImageFromS3(String imageAddress){
        String key = getKeyFromImageAddress(imageAddress);
        try{
            amazonS3.deleteObject(new DeleteObjectRequest(bucket, key));
        }catch (Exception e){
            log.error("Failed to delete image from S3. Key: {}, Error: {}", key, e.getMessage(), e);
        }
    }

    /**
     * 이미지 public URL에서 S3 key(파일 경로) 추출
     */
    private String getKeyFromImageAddress(String imageAddress){
        try{
            URL url = new URL(imageAddress);
            String decodingKey = URLDecoder.decode(url.getPath(), "UTF-8");
            return decodingKey.substring(1); // 맨 앞의 '/' 제거
        }catch (MalformedURLException | UnsupportedEncodingException e){
            throw new BusinessException(IO_EXCEPTION_ON_IMAGE_DELETE);
        }
    }
}
