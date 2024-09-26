package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class PointServiceImpl implements PointService {

    @Autowired
    private PointHistoryTable pointHistoryTable;

    @Autowired
    private UserPointTable userPointTable;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public UserPoint charge(long id, long amount) {
        lock.writeLock().lock();

        try {
            PointRequest.validate(id, amount);

            AtomicReference<UserPoint> userPoint = new AtomicReference<>(userPointTable.selectById(id));

            if (amount > 1_000_000) {
                throw new RuntimeException("한번에 1,000,000까지 충전할 수 있습니다.");
            }

            long totalPoint = userPoint.get().point() + amount;

            userPoint.set(userPointTable.insertOrUpdate(id, totalPoint));
            pointHistoryTable.insert(id, totalPoint, TransactionType.CHARGE, System.currentTimeMillis());

            return userPoint.get();
        } finally {
            lock.writeLock().unlock();
        }

    }

    @Override
    public UserPoint use(long id, long amount) {
        lock.writeLock().lock();

        try {
            PointRequest.validate(id, amount);

            AtomicReference<UserPoint> userPoint = new AtomicReference<>(userPointTable.selectById(id));

            if (amount > userPoint.get().point()) {
                throw new RuntimeException("사용 하려는 포인트가 가지고 있는 포인트 보다 많습니다.");
            }

            long totalPoint = userPoint.get().point() - amount;

            userPoint.set(userPointTable.insertOrUpdate(id, totalPoint));
            pointHistoryTable.insert(id, totalPoint, TransactionType.USE, System.currentTimeMillis());

            return userPoint.get();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public UserPoint getUserPoints(long id) { return userPointTable.selectById(id); }

    @Override
    public List<PointHistory> getPointHistories(long id) {
        return pointHistoryTable.selectAllByUserId(id);
    }
}