package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(rollbackFor = Exception.class)
    public UserPoint chargeUserPoint(long id, long amount) throws Exception {
        Lock lock = getLockForId(id);
        lock.lock();

        try {
            UserPoint userPoint = userPointTable.selectById(id);
            long currPoint = userPoint.point();

            if(currPoint + amount >= 10000){
                throw new Exception("포인트가 최대 잔고인 10000원 이상입니다.");
            }

            userPointTable.insertOrUpdate(id, currPoint + amount);
            pointHistoryTable.insert(id, amount, TransactionType.CHARGE, System.currentTimeMillis());

            return userPointTable.selectById(id);
        } finally {
            lock.unlock();
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public UserPoint useUserPoint(long id, long amount) throws Exception {
        Lock lock = getLockForId(id);
        lock.lock();

        try {
            UserPoint userPoint = userPointTable.selectById(id);
            long currPoint = userPoint.point();

            if (currPoint < amount) {
                throw new Exception("포인트가 부족합니다.");
            }

            userPointTable.insertOrUpdate(id, currPoint - amount);
            pointHistoryTable.insert(id, amount, TransactionType.USE, System.currentTimeMillis());

            return userPointTable.selectById(id);
        } finally {
            lock.unlock();
        }
    }
}
