package io.hhplus.tdd.unit;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.PointServiceImpl;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class PointUnitTest {

    @Mock
    private PointHistoryTable pointHistoryTable;

    @Mock
    private UserPointTable userPointTable;

    @InjectMocks
    private PointServiceImpl pointService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("충전 성공 했습니다.")
    void success_pointCharge() {
        long userId = 1L;
        long amount = 5_000L;
        long current = System.currentTimeMillis();
        UserPoint mockUserPoint = new UserPoint(userId, 1_000L, current);

        when(userPointTable.selectById(userId)).thenReturn(mockUserPoint);
        when(userPointTable.insertOrUpdate(anyLong(), anyLong())).thenReturn(new UserPoint(userId, 6_000L, current));

        UserPoint result = pointService.charge(userId, amount);

        assertEquals(6_000L, result.point());
        verify(pointHistoryTable).insert(eq(userId), eq(6_000L), eq(TransactionType.CHARGE), anyLong());
    }

    @Test
    @DisplayName("포인트는 한도 초과로 RuntimeException 발생합니다.")
    void fail_ifExceedMaximumAmount() {
        long userId = 1L;
        long amount = 1_500_000L;

        RuntimeException exception = assertThrows(RuntimeException.class, () -> pointService.charge(userId, amount));

        assertEquals("한번에 1,000,000까지 충전할 수 있습니다.", exception.getMessage());
        verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());
        verify(pointHistoryTable, never()).insert(anyLong(), anyLong(), any(), anyLong());
    }

    @Test
    @DisplayName("포인트 사용 성공 했습니다.")
    void success_pointUse() {
        long userId = 1L;
        long amount = 300_000L;
        long current = System.currentTimeMillis();
        UserPoint mockUserPoint = new UserPoint(userId, 500_000L, current);

        when(userPointTable.selectById(userId)).thenReturn(mockUserPoint);
        when(userPointTable.insertOrUpdate(anyLong(), anyLong())).thenReturn(new UserPoint(userId, 200_000L, current));

        UserPoint result = pointService.use(userId, amount);

        assertEquals(200_000L, result.point());
        verify(pointHistoryTable).insert(eq(userId), eq(200_000L), eq(TransactionType.USE), anyLong());
    }

    @Test
    @DisplayName("사용하려는 포인트가 사용자가 가지고 있는 포인트를 넘어서 RuntimeException이 발생합니다.")
    void fail_ifExceedingUserPoints() {
        long userId = 1L;
        long amount = 600_000L;
        long current = System.currentTimeMillis();
        UserPoint mockUserPoint = new UserPoint(userId, 500_000L, System.currentTimeMillis());

        when(userPointTable.selectById(userId)).thenReturn(mockUserPoint);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> pointService.use(userId, amount));

        assertEquals("사용 하려는 포인트가 가지고 있는 포인트 보다 많습니다.", exception.getMessage());
        verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());
        verify(pointHistoryTable, never()).insert(anyLong(), anyLong(), any(), anyLong());
    }

    @Test
    @DisplayName("사용자의 포인트를 성공적으로 반환합니다.")
    void return_getUserPoints() {
        long userId = 1L;
        UserPoint mockUserPoint = new UserPoint(userId, 500_000L, System.currentTimeMillis());

        when(userPointTable.selectById(userId)).thenReturn(mockUserPoint);

        UserPoint result = pointService.getUserPoints(userId);

        assertEquals(500_000L, result.point());
        verify(userPointTable).selectById(userId);
    }

    @Test
    @DisplayName("사용자의 포인트 내역을 성공적으로 반환합니다.")
    void return_getPointHistories() {
        long id = 1L;
        long userId = 1L;
        long current = System.currentTimeMillis();
        List<PointHistory> mockHistories = List.of(new PointHistory(id, userId, 500_000L, TransactionType.CHARGE, current));

        when(pointHistoryTable.selectAllByUserId(userId)).thenReturn(mockHistories);

        List<PointHistory> result = pointService.getPointHistories(userId);

        assertEquals(1, result.size());
        verify(pointHistoryTable).selectAllByUserId(userId);
    }
}
