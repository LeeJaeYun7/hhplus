package io.hhplus.tdd.point.controller;

import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.service.PointService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/point")
public class PointController {

    private static final Logger log = LoggerFactory.getLogger(PointController.class);

    private final PointService pointService;

    public PointController(PointService pointService) {
        this.pointService = pointService;
    }

    /**
     * TODO - 특정 유저의 포인트를 조회하는 기능을 작성해주세요.
     */
    @GetMapping("{id}")
    public ResponseEntity<UserPoint> retrieveUserPoint(@PathVariable long id) {
        UserPoint userPoint = pointService.getUserPoint(id);
        return ResponseEntity.ok().body(userPoint);
    }

    /**
     * TODO - 특정 유저의 포인트 충전/이용 내역을 조회하는 기능을 작성해주세요.
     */
    @GetMapping("{id}/histories")
    public ResponseEntity<List<PointHistory>> retrieveUserPointHistories(@PathVariable long id) {
        List<PointHistory> userPointHistories = pointService.getUserPointHistories(id);
        return ResponseEntity.ok().body(userPointHistories);
    }

    /**
     * TODO - 특정 유저의 포인트를 충전하는 기능을 작성해주세요.
     */
    @PatchMapping("{id}/charge")
    public ResponseEntity<UserPoint> chargeUserPoint(@PathVariable long id, @RequestBody long amount) {
        UserPoint userPoint = pointService.chargeUserPoint(id, amount);
        return ResponseEntity.ok().body(userPoint);
    }

    /**
     * TODO - 특정 유저의 포인트를 사용하는 기능을 작성해주세요.
     */
    @PatchMapping("{id}/use")
    public ResponseEntity<UserPoint> useUserPoint(@PathVariable long id, @RequestBody long amount) throws Exception {
        UserPoint userPoint = pointService.useUserPoint(id, amount);
        return ResponseEntity.ok().body(userPoint);
    }
}
