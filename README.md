

```
1) 동시성 제어 방식에 대한 보고서

- 제가 작성한 동시성 제어 방식은 다음과 같습니다

(1) 이 요구 사항에서 동시성 제어는 하나의 유저 id에 대해 포인트 사용, 포인트 충전에 대한 동시성 처리가 필요하다고 판단했습니다.

(2) 이를 위해 포인트 사용, 포인트 충전 각각의 메소드이 시작 시점에 lock을 걸고, 끝나는 시점에 lock을 해제하는 방식으로
    동시성을 제어했습니다.

(3) lock은 ConcurrentHashMap을 통해서 관리했습니다.

※ 왜 ConcurrentHashMap인가?
- ConcurrentHashMap은 Thread-safe를 제공함과 동시에 Entry 기준으로 잠금을 하기 때문에
  전체 테이블 기준으로 잠금을 하는 HashTable에 비교해서 높은 성능을 제공합니다. 
  (참고 자료: https://tecoble.techcourse.co.kr/post/2021-11-26-hashmap-hashtable-concurrenthashmap/)


## 동시성 제어 분석 보고서
동시성 제어 방식에 대한 상세한 분석 및 보고서는 https://github.com/LeeJaeYun7/hhplus-week-01/blob/master/CONCURRENCY_CONTROL.md 파일에서 확인할 수 있습니다.

```
