package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.*;

public class PointServiceTest {
    private PointService pointService;

    private UserPointTable pointTable;

    private PointHistoryTable pointHistoryTable;

    private ConcurrentHashMap<Long, Lock> lockMap;

    @BeforeEach
    public void setUp() {
        pointTable = new UserPointTable();
        pointHistoryTable = new PointHistoryTable();
        lockMap = new ConcurrentHashMap<>();
        pointService = new PointService(pointTable, pointHistoryTable, lockMap);
    }

    @Nested
    @DisplayName("포인트를 조회할 때")
    class 포인트를_조회할_때 {

        @Test
        void id가_존재하는_경우_성공한다() {
            pointTable.insertOrUpdate(1L, 1000);

            UserPoint result = pointService.getUserPoint(1L);

            assertEquals(1L, result.id());
            assertEquals(1000, result.point());
        }

        @Test
        void id가_없는경우_빈_객체를_반환한다() {
            UserPoint result = pointService.getUserPoint(999L);

            assertEquals(999L, result.id());
            assertEquals(0, result.point());
        }
    }

    @Nested
    @DisplayName("포인트 히스토리를 조회할 때")
    class 포인트_히스토리를_조회할_때 {

        @Test
        void id가_존재하는_경우_성공한다() {
            pointHistoryTable.insert(1L, 100, TransactionType.CHARGE, System.currentTimeMillis());
            pointHistoryTable.insert(1L, 100, TransactionType.USE, System.currentTimeMillis());

            List<PointHistory> result = pointService.getUserPointHistories(1L);

            assertEquals(1L, result.get(0).userId());
            assertEquals(100, result.get(0).amount());
            assertEquals(TransactionType.CHARGE, result.get(0).type());

            assertEquals(1L, result.get(1).userId());
            assertEquals(100, result.get(1).amount());
            assertEquals(TransactionType.USE, result.get(1).type());
        }

        @Test
        void id가_없는경우_빈_리스트를_반환한다() {
            List<PointHistory> result = pointService.getUserPointHistories(999L);
            assertEquals(0, result.size());
        }
    }

    @Nested
    @DisplayName("새로운 락을 생성하고 조회할 때")
    class 새로운_락을_생성하고_조회할때 {
        @Test
        public void 새로운_락을_생성하는뎨_성공한다() {
            // Given
            long id = 1L;

            // When
            Lock lock = pointService.getLockForId(id);

            // Then
            assertNotNull(lock, "Lock이 null이 아님을 확인");
            assertTrue(lock instanceof ReentrantLock, "생성된 Lock이 ReentrantLock 타입인지 확인");
            assertSame(lock, lockMap.get(id), "Lock이 lockMap에 저장된 것과 동일한지 확인");
        }
        @Test
        public void 생성된_락을_조회하는데_성공한다() {
            // Given
            long id = 1L;
            ReentrantLock existingLock = new ReentrantLock();
            lockMap.put(id, existingLock);  // 미리 맵에 락을 넣어둠

            // When
            Lock lock = pointService.getLockForId(id);

            // Then
            assertNotNull(lock, "Lock이 null이 아님을 확인");
            assertSame(existingLock, lock, "기존에 있던 Lock이 반환되는지 확인");
        }
    }

    @Nested
    @DisplayName("포인트를 충전할때")
    class 포인트를_충전할때 {
        @Test
        public void 포인트_충전에_성공한다() {

            // Given
            long id = 1L;
            pointTable.insertOrUpdate(id, 1000L);

            ReentrantLock lock = new ReentrantLock();
            lockMap.put(id, lock);  // 미리 맵에 락을 넣어둠

            // When
            long amountToCharge = 500L;
            UserPoint result = pointService.chargeUserPoint(id, amountToCharge);

            // Then
            assertNotNull(result);  // 결과가 null이 아닌지 확인
            assertEquals(1500L, result.point());  // 최종 포인트가 1500인지 확인
        }
    }

    @Nested
    @DisplayName("포인트를 사용할때")
    class 포인트를_사용할때 {
        @Test
        public void 포인트_사용에_성공한다() throws Exception {

            // Given
            long id = 1L;
            pointTable.insertOrUpdate(id, 1000L);

            ReentrantLock lock = new ReentrantLock();
            lockMap.put(id, lock);  // 미리 맵에 락을 넣어둠

            // When
            long amountToUse = 500L;
            UserPoint result = pointService.useUserPoint(id, amountToUse);

            // Then
            assertNotNull(result);  // 결과가 null이 아닌지 확인
            assertEquals(500L, result.point());  // 최종 포인트가 1500인지 확인
        }

        @Test
        public void 포인트_사용에_실패한다() {
            // Given
            long id = 1L;
            pointTable.insertOrUpdate(id, 1000L);

            ReentrantLock lock = new ReentrantLock();
            lockMap.put(id, lock);  // 미리 맵에 락을 넣어둠

            // When
            long amountToUse = 1500L;

            // Then
            assertThrows(Exception.class, () -> pointService.useUserPoint(id, amountToUse));
        }
    }

    @Nested
    @DisplayName("포인트 충전과 사용이 동시에 요청될때")
    class 포인트_충전과_사용이_동시에_요청될때 {
        @Test
        public void 포인트_충전과_사용이_동시에_요청될때_성공한다() throws InterruptedException, ExecutionException {
            long id = 1L;
            long initialAmount = 1000L;

            // 초기 포인트 설정
            pointService.chargeUserPoint(id, initialAmount);

            // 700원 충전, 1000원 충전, 500원 차감 요청을 동시에 처리하는 스레드 생성
            ExecutorService executor = Executors.newFixedThreadPool(3);
            Callable<UserPoint> charge700 = () -> pointService.chargeUserPoint(id, 700L);
            Callable<UserPoint> charge1000 = () -> pointService.chargeUserPoint(id, 1000L);
            Callable<UserPoint> use500 = () -> pointService.useUserPoint(id, 500L);

            // 작업들을 Future 리스트로 실행
            Future<UserPoint> futureCharge700 = executor.submit(charge700);
            Future<UserPoint> futureCharge1000 = executor.submit(charge1000);
            Future<UserPoint> futureUse500 = executor.submit(use500);

            // 모든 작업이 완료되기를 기다림
            futureCharge700.get();
            futureCharge1000.get();
            futureUse500.get();

            // Executor 서비스 종료
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);

            // 최종 포인트 검증
            UserPoint finalUserPoint = pointService.chargeUserPoint(id, 0);  // 최종 포인트 확인을 위해 0원 충전
            long expectedFinalPoint = 1000L + 700L + 1000L - 500L;
            assertEquals(expectedFinalPoint, finalUserPoint.point(), "최종 포인트가 예상과 일치하지 않습니다.");
        }
    }
}
