package konkuk.thip.post.adapter.out.persistence;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import konkuk.thip.post.application.port.out.PostLikeRedisCommandPort;
import konkuk.thip.post.application.port.out.PostLikeRedisQueryPort;
import konkuk.thip.post.domain.PostType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
@Component
@RequiredArgsConstructor
public class PostLikeRedisAdapter implements PostLikeRedisCommandPort, PostLikeRedisQueryPort {

    private final RedisTemplate<String, Integer> redisTemplate; //카운트용
    private final StringRedisTemplate stringRedisTemplate; // Set 기록용
    private static final Duration TTL = Duration.ofMinutes(10);

    @Value("${app.redis.post-like-count-prefix}")
    private String postLikeCountPrefix;
    @Value("${app.redis.post-like-record-prefix}")
    private String postLikeRecordPrefix;

    @Override
    public Integer getLikeCount(PostType postType, Long postId, Integer dbLikeCount) {
        String redisKey = makeLikeCountRedisKey(postType, postId);
        Integer likeCount = redisTemplate.opsForValue().get(redisKey);
        if (likeCount != null) {
            return likeCount; //cache hit
        }

        // cache miss 캐시에 없으면 DB값을 캐시에 저장 후 리턴
        redisTemplate.opsForValue().set(redisKey, dbLikeCount, TTL);
        return dbLikeCount;
    }

    @Override
    public Map<String, Integer> getAllLikeCounts() {
        // 모든 좋아요 카운트 키 검색
        Set<String> keys = redisTemplate.keys(postLikeCountPrefix + "*");
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Integer> values = redisTemplate.opsForValue().multiGet(keys);

        Map<String, Integer> result = new HashMap<>();
        List<String> keyList = new ArrayList<>(keys);

        // key와 value를 매핑하여 맵 생성
        for (int i = 0; i < keyList.size(); i++) {
            if (values.get(i) != null) {
                result.put(keyList.get(i), values.get(i));
            }
        }
        return result; //각 키의 좋아요 수를 맵으로 반환
    }

    @Override
    public void updateLikeCount(PostType postType, Long postId, Integer likeCount, boolean isLike) {
        String redisKey = makeLikeCountRedisKey(postType, postId);
        // 키가 없으면 getLikeCount로 초기화
        if (!redisTemplate.hasKey(redisKey)) {
            getLikeCount(postType,postId,likeCount);
        }
        if(isLike) incrementLikeCount(postType,postId);
        else decrementLikeCount(postType,postId);
    }

    @Override
    public void bulkResetLikeCounts(Set<String> keysToReset) {
        if (keysToReset.isEmpty()) {
            return;
        }

        // Pipeline을 사용하여 일괄적으로 값을 0으로 설정
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (String key : keysToReset) {
                redisTemplate.opsForValue().set(key, 0);
            }
            return null;
        });
    }

    @Override
    public boolean isLikedPostByUser(Long userId, Long postId) {
        // Post ID별로 Set을 구성하고, Set 안에 userId가 있는지 확인합니다.
        String recordKey = makeRecordRedisKey(postId);

        // SISMEMBER 명령어 실행: Set 안에 userId(멤버)가 존재하는지 확인
        // redisTemplate.opsForSet().isMember(key, member)는 Boolean을 반환합니다.
        Boolean isMember = stringRedisTemplate.opsForSet().isMember(recordKey, userId.toString());

        return isMember != null && isMember;
    }

    @Override
    public void addLikeRecordToSet(Long userId, Long postId) {
        String recordKey = makeRecordRedisKey(postId);
        // SADD 명령어: Set에 userId를 추가
        stringRedisTemplate.opsForSet().add(recordKey, userId.toString());
    }

    @Override
    public void removeLikeRecordFromSet(Long userId, Long postId) {
        String recordKey = makeRecordRedisKey(postId);
        // SREM 명령어: Set에서 userId를 제거
        stringRedisTemplate.opsForSet().remove(recordKey, userId.toString());
    }

    private void incrementLikeCount(PostType postType, Long postId) {
        String redisKey = makeLikeCountRedisKey(postType, postId);
        redisTemplate.opsForValue().increment(redisKey);
        redisTemplate.expire(redisKey, TTL);
    }

    private void decrementLikeCount(PostType postType, Long postId) {
        String redisKey = makeLikeCountRedisKey(postType, postId);
        redisTemplate.opsForValue().decrement(redisKey);
        redisTemplate.expire(redisKey, TTL);
    }

    private String makeLikeCountRedisKey(PostType type, Long postId) {
        return postLikeCountPrefix + type.name() + ":" + postId;
    }

    private String makeRecordRedisKey(Long postId) {
        return postLikeRecordPrefix + postId;
    }

}
