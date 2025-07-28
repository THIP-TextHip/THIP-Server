package konkuk.thip.vote.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("[단위] VoteItem 단위 테스트")
class VoteItemTest {

    @Test
    @DisplayName("투표수가 0개일 경우 모든 비율은 0이 된다")
    void calculatePercentages_allZero() {
        List<Integer> counts = List.of(0, 0, 0);

        List<Integer> result = VoteItem.calculatePercentages(counts);

        assertThat(result).containsExactly(0, 0, 0);
    }

    @Test
    @DisplayName("투표수가 하나만 있을 때 100%")
    void calculatePercentages_singleCandidate() {
        List<Integer> counts = List.of(10);

        List<Integer> result = VoteItem.calculatePercentages(counts);

        assertThat(result).containsExactly(100);
    }

    @Test
    @DisplayName("투표수가 균등할 경우 비율이 100%를 합산하고 균등하게 분배된다")
    void calculatePercentages_equalCounts() {
        List<Integer> counts = List.of(3, 3, 3);

        List<Integer> result = VoteItem.calculatePercentages(counts);

        int sum = result.stream().mapToInt(Integer::intValue).sum();

        assertThat(sum).isEqualTo(100);
        assertThat(result).containsExactly(34, 33, 33); // 소수점 오차 보정된 분배
    }

    @Test
    @DisplayName("투표수가 다를 경우 비율이 올바르게 계산되고 합계가 100이 된다")
    void calculatePercentages_variedCounts() {
        List<Integer> counts = List.of(3, 3, 4); // 합계 10

        List<Integer> result = VoteItem.calculatePercentages(counts);

        int sum = result.stream().mapToInt(Integer::intValue).sum();

        assertThat(sum).isEqualTo(100);
        // 30, 30, 40 순서일 것으로 기대
        assertThat(result).containsExactly(30, 30, 40);
    }

    @Test
    @DisplayName("순서가 유지되는지 확인")
    void calculatePercentages_orderIsPreserved() {
        List<Integer> counts = List.of(1, 2, 7); // 합계 10

        List<Integer> result = VoteItem.calculatePercentages(counts);

        // 합계 확인
        int sum = result.stream().mapToInt(Integer::intValue).sum();
        assertThat(sum).isEqualTo(100);

        // 10%, 20%, 70% 순서를 유지
        assertThat(result).containsExactly(10, 20, 70);
    }
}