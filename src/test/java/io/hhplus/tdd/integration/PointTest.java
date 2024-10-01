package io.hhplus.tdd.integration;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class PointTest {


    @Autowired
    private PointHistoryTable pointHistoryTable;

    @Autowired
    private UserPointTable userPointTable;

    @Autowired
    private PointServiceImpl pointService;

    @Test
    @DisplayName("포인트 내역 리스트가 존재하면 반환 합니다.")
    void return_ifExistsPointHistory() {
        // given: 포인트 내역을 생성
        long current = System.currentTimeMillis();
        PointHistory pointHistory1 = new PointHistory(1L, 1L, 1000L, TransactionType.CHARGE, current);
        PointHistory pointHistory2 = new PointHistory(2L, 1L, 2000L, TransactionType.CHARGE, current);

        pointHistoryTable.insert(1L, 1000L, TransactionType.CHARGE, current);
        userPointTable.insertOrUpdate(1L, 1000L);

        pointHistoryTable.insert(1L, 2000L, TransactionType.CHARGE, current);
        userPointTable.insertOrUpdate(1L, 2000L);

        List<PointHistory> expectedList = Arrays.asList(pointHistory1, pointHistory2);

        // when
        List<PointHistory> actualList = pointService.getPointHistories(1L);

        // then
        assertEquals(2, actualList.size());
        assertEquals(expectedList, actualList);
    }

    @Test
    @DisplayName("포인트 충전이 정상 작동 합니다.")
    void success_chargePoint() {
        // given: 통합테스트 ( 서비스 안의 Table를 통해 포인트가 늘어나는 테스트 )
        UserPoint actualUserPoint = pointService.charge(1L, 2000L);

        // then
        assertEquals(2000L, actualUserPoint.point()); // 포인트 비교
    }

    @Test
    @DisplayName("포인트 사용이 정상 작동 합니다.")
    void success_usePoint() {
        // given: 통합테스트 ( 서비스 안의 Table를 통해 포인트가 줄어드는 테스트 )
        UserPoint initUserPoint = new UserPoint(1L, 2000L, System.currentTimeMillis());
        pointService.charge(initUserPoint.id(), initUserPoint.point());

        // when
        UserPoint actualUserPoint = pointService.charge(1L, 2000L);

        // then
        assertEquals(4000L, actualUserPoint.point()); // 포인트 비교
    }

}
