package io.hhplus.tdd.validator;

import io.hhplus.tdd.point.PointRequest;

public class PointValidator {

    public static void validate(PointRequest pointRequest) {
        validateId(pointRequest.getId()); // id에 관한 검증
        validateAmount(pointRequest.getAmount()); // 포인트에 관한 검증
    }

    public static void validateId(Long id) {
        if (id == null) throw new RuntimeException("id가 null입니다.");
        if (id <= 0) throw new RuntimeException("id값이 0보다 작거나 같습니다.");
    }

    public static void validateAmount(Long amount) {
        if (amount == null) throw new RuntimeException("amount가 null입니다.");
        if (amount <= 0) throw new RuntimeException("포인트가 0보다 작거나 같습니다.");
    }
}
