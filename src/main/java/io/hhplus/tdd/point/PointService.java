package io.hhplus.tdd.point;

import java.util.List;


public interface PointService {

    // 특정 사용자의 포인트를 충전한다.
    UserPoint charge(long id, long amount);

    // 특정 사용자의 포인트를 사용한다.
    UserPoint use(long id, long amount);

    // 특정 사용자의 포인트를 조회한다.
    UserPoint getUserPoints(long id);

    // 특정 사용자의 포인트 내역을 조회한다.
    List<PointHistory> getPointHistories(long id);

}
