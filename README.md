Java Stream API 성능 개선 사례 발표
📌 개요
Java Stream API를 활용한 코드의 성능을 측정하고 개선한 3가지 사례를 소개합니다.

🎯 사례 1: ParallelStream에서 forEach vs collect 비교
📂 파일: StreamAPI4.java
❌ 비효율적인 코드
javanumbers.parallelStream()
    .map(n -> n * n)
    .forEach(result -> {
        // forEach는 순서 보장 안 됨
    });
✅ 개선된 코드
javaList<Integer> squaredNumbers = numbers.parallelStream()
    .map(n -> n * n)
    .collect(Collectors.toList());
```

### 💡 왜 개선되었나?

1. **forEach의 문제점**
   - `parallelStream()`에서 `forEach()`는 순서를 보장하지 않음
   - 각 스레드가 독립적으로 동작하면서 동기화 오버헤드 발생
   - 결과를 수집하지 않고 단순히 실행만 하므로 비효율적

2. **collect의 장점**
   - 병렬 처리 결과를 효율적으로 수집
   - 내부적으로 최적화된 리듀싱 전략 사용
   - 스레드 안전하게 결과를 병합

### 📊 성능 비교 결과
```
=== 데이터 크기: 1000000개 ===

[forEach 방식] 실행 시간: XXms
[collect 방식] 실행 시간: YYms  (더 빠름!)

🎯 사례 2: ParallelStream에서 Thread-Safe 컬렉션 사용
📂 파일: StreamAPI3.java
❌ 비효율적인 코드 (주석 처리됨)
javaList<Integer> results = new ArrayList<>();
numbers.parallelStream().forEach(number -> {
    results.add(number * 2);  // ⚠️ Thread-Safe 하지 않음!
});
✅ 개선된 코드
javaList<Integer> results2 = new CopyOnWriteArrayList<>();
numbers.parallelStream().forEach(number -> {
    results2.add(number * 2);  // ✅ Thread-Safe!
});
💡 왜 개선되었나?

ArrayList의 문제점

멀티 스레드 환경에서 동시에 add() 호출 시 Race Condition 발생
데이터 손실 또는 ArrayIndexOutOfBoundsException 발생 가능
예상치 못한 결과 발생


CopyOnWriteArrayList의 장점

쓰기 작업 시 내부 배열을 복사하여 Thread-Safe 보장
읽기 작업은 락 없이 수행 가능
병렬 처리 환경에서 안전하게 사용 가능



⚠️ 주의사항

CopyOnWriteArrayList는 쓰기가 적고 읽기가 많을 때 유리
더 나은 방법: collect(Collectors.toList()) 사용 권장!


🎯 사례 3: Stream 체이닝 최적화
📂 파일: OverusingTest.java
❌ 비효율적인 코드 (test1)
javaList<String> result = names.stream()
    .filter(name -> name.startsWith("A"))      // 첫 번째 필터
    .filter(name -> name.length() > 3)         // 두 번째 필터
    .map(String::toUpperCase)                  // 첫 번째 변환
    .map(name -> name + " is a name")          // 두 번째 변환
    .toList();
✅ 개선된 코드 (test2)
javaList<String> result = names.stream()
    .filter(name -> name.startsWith("A") && name.length() > 3)  // 필터 통합
    .map(name -> name.toUpperCase() + " is a name")             // 변환 통합
    .toList();
```

### 💡 왜 개선되었나?

1. **체이닝이 많을 때의 문제점**
   - 각 중간 연산마다 내부적으로 새로운 스트림 파이프라인 생성
   - 불필요한 람다 호출 증가
   - 데이터가 여러 단계를 거치며 오버헤드 발생

2. **통합의 장점**
   - **필터 통합**: 조건 검사를 한 번에 수행
   - **맵 통합**: 변환 작업을 한 번에 처리
   - 중간 연산 횟수 감소로 성능 향상

### 📊 성능 비교 결과
```
[test1 - 체이닝 많음]
결과: [ALICE is a name]
걸린 시간(나노초): XXX ns

[test2 - 체이닝 최적화]
결과: [ALICE is a name]
걸린 시간(나노초): YYY ns  (더 빠름!)

📋 전체 성능 비교 (case03_compare.java)
실험 조건

작은 데이터: 5개 (출력 포함으로 병렬화 오버헤드 확인)
큰 데이터: 1,000,000개 (실제 성능 개선 확인)

핵심 발견

작은 데이터셋에서 parallelStream + println

스레드 생성/관리 오버헤드가 더 큼
출력 작업의 동기화로 인한 성능 저하


큰 데이터셋에서 parallelStream + collect

병렬 처리의 진정한 이점 발휘
효율적인 결과 수집




✨ 결론 및 Best Practices
1️⃣ ParallelStream 사용 시

✅ collect(Collectors.toList())를 사용하여 결과 수집
❌ forEach로 외부 컬렉션에 직접 추가하지 말 것

2️⃣ Thread-Safety

✅ 병렬 스트림에서는 Thread-Safe 컬렉션 사용
✅ 가능하면 collect()를 사용하여 안전하게 수집

3️⃣ Stream 체이닝

✅ 가능한 한 중간 연산을 통합
✅ 필터 조건은 &&로 결합
✅ 맵 변환은 하나의 람다로 처리

4️⃣ 성능 측정

System.nanoTime()을 활용한 정확한 성능 측정
작은 데이터와 큰 데이터에서 모두 테스트
