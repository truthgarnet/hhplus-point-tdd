package io.hhplus.tdd.point;

import io.hhplus.tdd.validator.PointValidator;
import lombok.Getter;

@Getter
public class PointRequest {

    private Long id;
    private Long amount;

    public PointRequest(Long id, Long amount) {
        this.id = id;
        this.amount = amount;
    }

    private PointRequest(long id) {
        this.id = id;
    }

    public static PointRequest validate(long id, long amount) {
        PointRequest request = new PointRequest(id, amount);
        PointValidator.validate(request);
        return request;
    }

    public static PointRequest validateId(long id) {
        PointRequest request = new PointRequest(id);
        PointValidator.validate(request);
        return request;
    }

}
