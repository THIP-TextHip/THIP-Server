package konkuk.thip.post.application.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import konkuk.thip.post.application.port.out.PostLikeCountRedisCommandPort;
import konkuk.thip.post.application.port.out.PostLikeCountRedisQueryPort;
import konkuk.thip.post.application.service.handler.PostHandler;
import konkuk.thip.post.domain.PostType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Profile({"!test"})
public class PostLikeCountSyncToDBService {

    private final PostLikeCountRedisQueryPort redisQueryPort;
    private final PostLikeCountRedisCommandPort redisCommandPort;
    private final PostHandler postHandler;

    @Value("${app.redis.post-like-count-prefix}")
    private String postLikeCountPrefix;

    @Scheduled(fixedRate = 60000)// 1분마다 실행
    @Transactional
    public void syncLikeCountsToDB() {
        Map<String, Integer> allKeyToLikeCount = redisQueryPort.getAllLikeCounts();
        if (allKeyToLikeCount.isEmpty()) {
            return;
        }

        // 타입별 ID 및 좋아요수 맵으로 분리
        Map<PostType, List<Long>> typeToIdList = new HashMap<>();
        Map<String, Integer> keyToLikeCountToUpdate = new HashMap<>(); // DB에 업데이트할 키만 저장
        boolean hasUpdates = false;

        for (Map.Entry<String, Integer> entry : allKeyToLikeCount.entrySet()) {
            String key = entry.getKey();
            Integer likeCount = entry.getValue();

            if (likeCount == null || likeCount.intValue() == 0) continue;
            // 0보다 큰 값이 있으면 업데이트가 필요함을 기록
            hasUpdates = true;

            String[] parts = key.split(":");
            if (parts.length != 4) continue;
            PostType type = PostType.valueOf(parts[2]);
            Long postId = Long.valueOf(parts[3]);

            typeToIdList.computeIfAbsent(type, k -> new ArrayList<>()).add(postId);
            keyToLikeCountToUpdate.put(key, likeCount);
        }

        if (!hasUpdates) {
            return;
        }

        for (Map.Entry<PostType, List<Long>> entry : typeToIdList.entrySet()) {
            PostType type = entry.getKey();
            List<Long> ids = entry.getValue();

            // 도메인별 id 리스트 중 실제 존재하는 id만 필터링
            List<Long> existingIds = postHandler.findPostIdsByIds(type, ids);
            if (existingIds.isEmpty()) continue;
            // 해당 id와 좋아요 수만 맵으로 생성
            Map<Long, Integer> idToLikeCount = existingIds.stream()
                    .collect(Collectors.toMap(id -> id, id -> {
                        String redisKey = postLikeCountPrefix + type.name() + ":" + id;
                        return keyToLikeCountToUpdate.getOrDefault(redisKey, 0);
                    }));

            // 도메인별 벌크 좋아요 DB 업데이트
            postHandler.batchUpdateLikeCounts(type, idToLikeCount);
        }

        // 레디스 리셋
        Set<String> updatedKeys = keyToLikeCountToUpdate.keySet();
        redisCommandPort.bulkResetLikeCounts(updatedKeys);
    }
}
