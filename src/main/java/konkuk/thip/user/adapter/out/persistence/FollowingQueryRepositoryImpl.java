package konkuk.thip.user.adapter.out.persistence;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import konkuk.thip.user.adapter.out.jpa.QFollowingJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class FollowingQueryRepositoryImpl implements FollowingQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    // 주어진 userId 리스트에 대해 각 userId의 팔로워(구독자) 수를 집계하여 Map으로 반환
    public Map<Long, Integer> countByFollowingUserIds(List<Long> userIds) {

        QFollowingJpaEntity following = QFollowingJpaEntity.followingJpaEntity;

        List<Tuple> results = jpaQueryFactory
                .select(following.followingUserJpaEntity.userId, following.count())
                .from(following)
                .where(following.followingUserJpaEntity.userId.in(userIds))
                .groupBy(following.followingUserJpaEntity.userId)
                .fetch();

        // 결과를 Map<Long, Integer>로 변환
        return results.stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(following.followingUserJpaEntity.userId),
                        tuple -> tuple.get(following.count()).intValue()
                ));
    }
}
