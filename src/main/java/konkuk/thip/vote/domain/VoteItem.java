package konkuk.thip.vote.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@SuperBuilder
public class VoteItem extends BaseDomainEntity {

    private Long id;

    private String itemName;

    private int count;

    private Long voteId;

    public static VoteItem withoutId(String itemName, int count, Long voteId) {
        return VoteItem.builder()
                .id(null)
                .itemName(itemName)
                .count(count)
                .voteId(voteId)
                .build();
    }


    /**
     * 투표 항목의 비율을 계산
     * @param counts 각 투표 항목의 카운트 리스트
     * @return 각 항목의 백분율을 나타내는 리스트
     */
    public static List<Integer> calculatePercentages(List<Integer> counts) {
        int total = counts.stream().mapToInt(Integer::intValue).sum();
        int n = counts.size();
        List<Integer> result = new ArrayList<>(Collections.nCopies(n, 0));

        if (total == 0 || n == 0) {
            return result;
        }

        double[] fractional = new double[n];
        int sum = 0;

        for (int i = 0; i < n; i++) {
            double exact = counts.get(i) * 100.0 / total;
            int base = (int) exact; // 정수 부분
            result.set(i, base);
            fractional[i] = exact - base; // 소수 부분
            sum += base;
        }

        int remaining = 100 - sum;

        // fractional과 index를 묶어서 정렬
        List<int[]> order = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            order.add(new int[]{i, (int) (fractional[i] * 1_000_000)}); // 정밀도 위해 정수화
        }

        // fractional 값이 큰 순서대로 내림차순 정렬
        order.sort((a, b) -> Integer.compare(b[1], a[1]));

        // 남은 %를 fractional이 큰 순서대로 분배
        for (int i = 0; i < remaining; i++) {
            int index = order.get(i % n)[0];
            result.set(index, result.get(index) + 1);
        }

        return result;
    }
}
