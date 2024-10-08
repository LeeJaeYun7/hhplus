
## 프로젝트 소개
- 이 프로젝트는 항해플러스 백엔드 6기 코스의 1주차 과제입니다. </br>
- TDD(테스트 주도 개발)를 이용하여 포인트 관리 API를 구현합니다. </br> 

## 과제 필수 사항
- 프로젝트에 첨부된 설정 파일은 수정하지 않습니다. </br> 
- 테스트 케이스 작성 및 작성 이유를 주석으로 작성합니다. </br>
- 프로젝트 내의 주석을 참고하여 필요한 기능을 작성합니다. </br>
- 분산 환경은 고려하지 않습니다. </br>

## 요구 사항
- point 패키지의 TODO와 테스트 코드를 작성해주세요.

## API 엔드포인트
- PATCH /point/{id}/charge : 포인트를 충전한다. </br>
- PATCH /point/{id}/use : 포인트를 사용한다. </br>
- GET /point/{id} : 포인트를 조회한다.</br>
- GET /point/{id}/histories : 포인트 내역을 조회한다. </br>

## 기능 요구사항
- 잔고가 부족할 경우, 포인트 사용은 실패해야 합니다. </br>
- 동시에 여러 건의 포인트 충전, 이용 요청이 들어올 경우 순차적으로 처리되어야 합니다. </br>

## 구현 단계
 Default </br>
- /point 패키지 내에 PointService 기본 기능 작성 </br> 
- /database 패키지의 구현체는 수정하지 않고, 이를 활용해 기능을 구현 </br>
- 각 기능에 대한 단위 테스트 작성
- 총 4가지 기본 기능 (포인트 조회, 포인트 충전/사용 내역 조회, 충전, 사용) 구현 </br> 
  
Step 1 </br> 
- 포인트 충전, 사용에 대한 정책 추가 (잔고 부족, 최대 잔고 등) </br>
- 동시에 여러 요청이 들어오더라도 순서대로 (혹은 한번에 하나의 요청씩만) 제어될 수 있도록 리팩토링 </br>
- 동시성 제어에 대한 통합 테스트 작성 </br> 

Step 2 </br>
- 동시성 제어 방식에 대한 분석 및 보고서 작성 (README.md에 포함) </br> 

## 동시성 제어 방식에 대한 보고서
- 동시성 제어 방식에 대한 상세한 분석 및 보고서는 [CONCURRENCY_CONTROL.md](https://github.com/LeeJaeYun7/hhplus-week-01/blob/master/CONCURRENCY_CONTROL.md) 파일에서 확인할 수 있습니다.
