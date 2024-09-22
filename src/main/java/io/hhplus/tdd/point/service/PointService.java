package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    public PointService(UserPointTable userPointTable, PointHistoryTable pointHistoryTable){
        this.userPointTable = userPointTable;
        this.pointHistoryTable = pointHistoryTable;
    }

    public UserPoint getUserPoint(long id){
        return userPointTable.selectById(id);
    }

    public List<PointHistory> getUserPointHistories(long id){
        return pointHistoryTable.selectAllByUserId(id);
    }

    public synchronized UserPoint chargeUserPoint(long id, long amount){
        UserPoint userPoint = userPointTable.selectById(id);
        long currPoint = userPoint.point();

        userPointTable.insertOrUpdate(id, currPoint+amount);
        pointHistoryTable.insert(id, amount, TransactionType.CHARGE, System.currentTimeMillis());

        return userPointTable.selectById(id);
    }

    public synchronized UserPoint useUserPoint(long id, long amount) throws Exception {
        UserPoint userPoint = userPointTable.selectById(id);
        long currPoint = userPoint.point();

        if(currPoint < amount) {
            throw new Exception();
        }

        userPointTable.insertOrUpdate(id, currPoint-amount);
        pointHistoryTable.insert(id, amount, TransactionType.USE, System.currentTimeMillis());

        return userPointTable.selectById(id);
    }
}
