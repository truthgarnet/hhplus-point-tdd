package io.hhplus.tdd.unit;

import io.hhplus.tdd.point.PointRequest;
import io.hhplus.tdd.validator.PointValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class PointValidatorTest {

    @Test
    @DisplayName("ID가 null일 때, RuntimeException가 발생합니다.")
    void validate_shouldThrowException_whenIdIsNull() {
        // given: ID가 null인 Request 객체 생성
        validate_shouldThrowRunTimeException(null, 100L);
    }

    @Test
    @DisplayName("ID가 0 이하이면, RuntimeException가 발생합니다.")
    void validate_shouldThrowException_whenIdUnderZero() {
        // given: amount가 0 이하인 Request 객체 생성
        validate_shouldThrowRunTimeException(0L, 100L);
    }

    @Test
    @DisplayName("포인트(amount)가 null일 때, RuntimeException가 발생합니다.")
    void validate_shouldThrowException_whenAmountIsNull() {
        // given: amount가 null인 Request 객체 생성
        validate_shouldThrowRunTimeException(1L, null);
    }

    @Test
    @DisplayName("포인트(amount)가 0 이하이면, RuntimeException가 발생합니다.")
    void validate_shouldThrowException_whenAmountUnderZero() {
        // given: amount가 0 이하인 Request 객체 생성
        validate_shouldThrowRunTimeException(1L, 0L);
    }

    // 검증 공통 함수
    public void validate_shouldThrowRunTimeException(Long id, Long amount) {
        // given: id, amount 중에 검증
        PointRequest invalid = new PointRequest(id, amount);

        // when & then
        assertThrows(RuntimeException.class, () -> PointValidator.validate(invalid));
    }

}
