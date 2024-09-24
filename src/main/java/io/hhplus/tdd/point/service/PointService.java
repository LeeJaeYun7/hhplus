package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class PointService {
    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;
    private final ConcurrentHashMap<Long, Lock> lockMap;
    public PointService(UserPointTable userPointTable, PointHistoryTable pointHistoryTable, ConcurrentHashMap<Long, Lock> lockMap){
        this.userPointTable = userPointTable;
        this.pointHistoryTable = pointHistoryTable;
        this.lockMap = lockMap;
    }

    public UserPoint getUserPoint(long id){
        return userPointTable.selectById(id);
    }

    public List<PointHistory> getUserPointHistories(long id){
        return pointHistoryTable.selectAllByUserId(id);
    }

    public Lock getLockForId(long id){
        lockMap.putIfAbsent(id, new ReentrantLock());
        return lockMap.get(id);
    }
    public UserPoint chargeUserPoint(long id, long amount){
        Lock lock = getLockForId(id);
        lock.lock();

        try {
            UserPoint userPoint = userPointTable.selectById(id);
            long currPoint = userPoint.point();

            userPointTable.insertOrUpdate(id, currPoint + amount);
            pointHistoryTable.insert(id, amount, TransactionType.CHARGE, System.currentTimeMillis());

            return userPointTable.selectById(id);
        } finally {
            lock.unlock();
        }
    }

    public UserPoint useUserPoint(long id, long amount) throws Exception {
        Lock lock = getLockForId(id);
        lock.lock();

        try {
            UserPoint userPoint = userPointTable.selectById(id);
            long currPoint = userPoint.point();

            if (currPoint < amount) {
                throw new Exception();
            }

            userPointTable.insertOrUpdate(id, currPoint - amount);
            pointHistoryTable.insert(id, amount, TransactionType.USE, System.currentTimeMillis());

            return userPointTable.selectById(id);
        } finally {
            lock.unlock();
        }
    }
}
