package io.hhplus.tdd.unit;

import io.hhplus.tdd.point.PointServiceImpl;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
public class PointConcurrentTest {

    @Autowired
    private PointServiceImpl pointService;

    @Test
    @DisplayName("충전 동시성 테스트")
    void test_chargeConcurrent() throws InterruptedException {

        // 스레드 풀 생성
        ExecutorService executor = Executors.newFixedThreadPool(10); // 왜 예시마다 10개의 쓰레드를 생성하는 걸까?
        AtomicInteger failedOperations = new AtomicInteger(0); // 실패한 작업 수를 세기 위한 변수

        // 10개의 쓰기 작업을 동시 실행
        for (int i = 0; i < 10; i++) {
            executor.submit(() -> {
                try {
                    pointService.charge(1L, 100);
                    System.out.println("charge point" + pointService.getUserPoints(1L));
                } catch (Exception e) {
                    System.out.println("에러:" + e.getMessage());
                    failedOperations.incrementAndGet(); // 예외가 발생하면 실패 카운트 증가
                }
            });
        }

        // 스레드 종료 후 결과 확인
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        // 최종 포인트 계산
        assertEquals(1000, pointService.getUserPoints(1L).point());
        assertEquals(0, failedOperations.get(), "There should be no failed operations");
    }

    @Test
    @DisplayName("사용 동시성 테스트")
    void test_useConcurrent() throws InterruptedException {

        UserPoint initPoint = pointService.charge(1L, 3000);

        // 스레드 풀 생성
        ExecutorService executor = Executors.newFixedThreadPool(10); // 왜 예시마다 10개의 쓰레드를 생성하는 걸까?
        AtomicInteger failedOperations = new AtomicInteger(0); // 실패한 작업 수를 세기 위한 변수

        // 10개의 쓰기 작업을 동시 실행
        for (int i = 0; i < 10; i++) {
            executor.submit(() -> {
                try {
                    pointService.use(1L, 100);
                    System.out.println("charge point" + pointService.getUserPoints(1L));
                } catch (Exception e) {
                    System.out.println("에러:" + e.getMessage());
                    failedOperations.incrementAndGet(); // 예외가 발생하면 실패 카운트 증가
                }
            });
        }

        // 스레드 종료 후 결과 확인
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        // 최종 포인트 계산
        assertEquals(2000, pointService.getUserPoints(1L).point());
        assertEquals(0, failedOperations.get(), "There should be no failed operations");
    }

    @Test
    @DisplayName("사용,충전 동시성 테스트")
    void test_useAndChargeConcurrent() throws InterruptedException {

        // 스레드 풀 생성
        ExecutorService executor = Executors.newFixedThreadPool(10); // 왜 예시마다 10개의 쓰레드를 생성하는 걸까?
        AtomicInteger failedOperations = new AtomicInteger(0); // 실패한 작업 수를 세기 위한 변수

        // 10개의 쓰기 작업을 동시 실행
        for (int i = 0; i < 10; i++) {
            final int idx = i;
            executor.submit(() -> {
                try {
                    if (idx % 2 == 0) {
                        pointService.charge(1L, 500);
                    } else {
                        pointService.use(1L, 100);
                    }
                } catch (Exception e) {
                    System.out.println("에러:" + e.getMessage());
                    failedOperations.incrementAndGet(); // 예외가 발생하면 실패 카운트 증가
                }
            });
        }

        // 스레드 종료 후 결과 확인
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        // 최종 포인트 계산 4000
        assertEquals(2000, pointService.getUserPoints(1L).point());
        assertEquals(0, failedOperations.get(), "There should be no failed operations");
    }
}
