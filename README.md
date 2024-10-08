# 동시성 해결 과정

Java 동시성을 검색하면 가장 많이 나오는 용어가 synchronized와 Lock입니다. 

비교도 많이 하는 모습을 볼 수 있습니다.

Lock에서는 많은 종류가 있지만, 빠르게 적용하기 위해서 많은 사람이 적용한 ReentrantLock을 사용 하며 진행했습니다.

## synchronized
synchronized는 실행할 때마다 결과 값이 달라지며, 의도한 대로 결과 값이 나오지 않았습니다.

![image](https://github.com/user-attachments/assets/d15c6b16-0e68-4a41-bfc8-a326e93a62d3)

<br>

synchronized는 한 프로세스 내에서만 동시성 제어를 할 수 있다고 나와있어서, 만약 서버가 다른 경우 동시성을 제어 하지 못 합니다.
![image2](https://github.com/user-attachments/assets/b91694ff-6e3c-4dc3-ba49-6d9037073ffc)

결과창을 확인 해보니 `순서`가 보장되고 있지 않습니다.
![image3](https://github.com/user-attachments/assets/9f05effd-6dbb-4e1b-9ea6-2aba7bad28bc)

저의 생각에서는 0번째가 먼저 진행되어서 포인트가 충전되고 1번째 포인트 사용되고 이런 식으로 진행될 것이라고 생각했던 것의 착각이었습니다.


## ReentrantLock

그래서 다른 방법인 Lock을 구현해보고자 했고, `ReentrantReadWriteLock` 을 사용하기로 했습니다.

``` java
private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(); 

public UserPoint charge(long id, long amount) {
        lock.writeLock().lock(); // 락 설정

        try {
            PointRequest.validate(id, amount);

            // 안전하게 객체를 읽고 수정 가능하게 함
            AtomicReference<UserPoint> userPoint = new AtomicReference<>(userPointTable.selectById(id));

            if (amount > 1_000_000) {
                throw new RuntimeException("한번에 1,000,000까지 충전할 수 있습니다.");
            }

            long totalPoint = userPoint.get().point() + amount;

            userPoint.set(userPointTable.insertOrUpdate(id, totalPoint));
            pointHistoryTable.insert(id, totalPoint, TransactionType.CHARGE, System.currentTimeMillis());

            return userPoint.get();
        } finally {
            lock.writeLock().unlock(); // 락을 해제
        }

    }
```

하지만, 여전히 오류가 발생하는 것을 볼 수 있습니다.
![image4](https://github.com/user-attachments/assets/af5cf38f-53d9-4ca9-b053-2667e9b11488)

그렇지만, synchronized와 다른 점이 있습니다.
그것은 바로 `에러가 발생하지 않는 것`을 볼 수 있습니다. 

그렇다면 실행하다가 무언가에 의해 뚝 끊긴다고 생각이 들어 실행하는 시간과 관련이 있을 것이라 생각했습니다. 
<br>
역시! 대기 시간이 충분하지 않아 모든 작업이 끝나기 전에 `스레드 풀이 종료`되는 것이었습니다. 

`timeout`을 제가 1초의 시간을 둬서, 작업이 끝나기도 전에 종료되었습니다.
![image5](https://github.com/user-attachments/assets/80835c9a-7d52-49a9-a1b2-3effc36dc764)

timeout을 `1초 -> 10초`로 변경한 후 실행한 결과 제가 의도 한대로, charge에서 차례대로 작동하는 것을 알 수 있었습니다.
![image6](https://github.com/user-attachments/assets/6280402d-844b-485b-9075-03cd82485f2e)


# 더욱 공부하고 싶은 것

- 데이터베이스에서 해결하는 Lock
- 자바 Lock의 다양한 종류
- 코드 베이스 단에서 Lock이 어떻게 돌아가는 지 Deep하게 배워보기
- Queue로 동시성 제어를 직접 구현해볼 수 있는 지
- 코치님들이 말씀하신 방법들 (Future/Lock/Flow/AtomicXx/..)

# 느낀점

회사 프로젝트가 그렇게 까지 트래픽이 몰리는 어플리케이션들도 아니여서 개발할 때, 동시성 제어에 대해서 생각해 본 적이 없어서 너무 낯설었습니다. 

동시성 제어를 생각 해보니, 동기/비동기, 블록킹, 프로세스….등등 알고 있지만, 자세하게 알지 못하는 용어들이 다가왔고, 공부할 것이 산더미처럼 다가왔습니다. 

과제를 진행 하면서 자세하게 알지 못했던 용어들을 배우는 시간이 되었고, 여태 진행했던 프로젝트 중에 동시성을 적용 시킬만한 프로젝트가 있을까? 현재의 프로젝트에 적용할 수 있을까? 에 대한 고민으로 식견이 넓어진 것 같아 뜻깊은 시간이었던 것 같습니다.
