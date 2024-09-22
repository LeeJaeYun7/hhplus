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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class PointServiceTest {

    @InjectMocks
    private PointService sut;

    @Mock
    private UserPointTable userPointTable;

    @Mock
    private PointHistoryTable pointHistoryTable;

    private PointService pointService;

    private UserPointTable pointTable;

    private PointHistoryTable historyTable;

    @BeforeEach
    public void setUp() {
        pointTable = new UserPointTable();
        historyTable = new PointHistoryTable();
        MockitoAnnotations.openMocks(this);
        pointService = new PointService(pointTable, historyTable);
    }

    @Nested
    @DisplayName("포인트를 조회할 때")
    class 포인트를_조회할_때 {

        @Test
        void id가_존재하는_경우_성공한다() {
            UserPoint userPoint = new UserPoint(1L, 100, System.currentTimeMillis());
            when(userPointTable.selectById(1L)).thenReturn(userPoint);

            UserPoint result = sut.getUserPoint(1L);

            assertEquals(userPoint.id(), result.id());
            assertEquals(userPoint.point(), result.point());
            assertEquals(userPoint.updateMillis(), result.updateMillis());
        }

        @Test
        void id가_없는경우_빈_객체를_반환한다() {
            when(userPointTable.selectById(999L)).thenReturn(UserPoint.empty(999L));

            UserPoint result = sut.getUserPoint(999L);

            assertEquals(999L, result.id());
            assertEquals(0, result.point());
        }
    }

    @Nested
    @DisplayName("포인트 히스토리를 조회할 때")
    class 포인트_히스토리를_조회할_때 {

        @Test
        void id가_존재하는_경우_성공한다() {
            PointHistory userPointHistory1 = new PointHistory(1L, 1L, 100, TransactionType.CHARGE, System.currentTimeMillis());
            PointHistory userPointHistory2 = new PointHistory(2L, 1L, 100, TransactionType.USE, System.currentTimeMillis());

            List<PointHistory> userPointHistories = List.of(userPointHistory1, userPointHistory2);
            when(pointHistoryTable.selectAllByUserId(1L)).thenReturn(userPointHistories);

            List<PointHistory> result = sut.getUserPointHistories(1L);

            assertEquals(userPointHistories.get(0).userId(), result.get(0).userId());
            assertEquals(userPointHistories.get(0).amount(), result.get(0).amount());
            assertEquals(userPointHistories.get(0).type(), result.get(0).type());

            assertEquals(userPointHistories.get(1).userId(), result.get(1).userId());
            assertEquals(userPointHistories.get(1).amount(), result.get(1).amount());
            assertEquals(userPointHistories.get(1).type(), result.get(1).type());
        }

        @Test
        void id가_없는경우_빈_리스트를_반환한다() {
            List<PointHistory> userPointHistories = List.of();
            when(pointHistoryTable.selectAllByUserId(999L)).thenReturn(userPointHistories);

            List<PointHistory> result = sut.getUserPointHistories(999L);

            assertEquals(0, result.size());
        }
    }

    @Nested
    @DisplayName("포인트를 충전할 때")
    class 포인트를_충전할_때 {

        @Test
        void 동시에_10개의_요청이_오는경우_순차적으로_처리된다() {

            int threadCount = 10;
            ExecutorService executorService = Executors.newFixedThreadPool(32);
            AtomicLong userId = new AtomicLong(1L);

            for(int i = 0; i < threadCount; i++) {

                executorService.submit(() -> {
                    long id;
                    synchronized(this){
                        id = userId.getAndIncrement();
                    }
                    try{
                        pointService.chargeUserPoint(id, 100);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                });
            }

            for(long i = 0; i < threadCount; i++){
                System.out.println(userPointTable.selectById(i+1).updateMillis());
            }
        }
    }

}
