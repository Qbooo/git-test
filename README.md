# Java Stream API 성능 개선 사례 발표

## 📌 개요
Java Stream API를 활용한 코드의 성능을 측정하고 개선한 3가지 사례를 소개합니다.

---

## 🎯 사례 1: ParallelStream에서 `forEach` vs `collect` 비교

### 📂 파일: `StreamAPI4.java`

### ❌ 비효율적인 코드
```java
numbers.parallelStream()
    .map(n -> n * n)
    .forEach(result -> {
        // forEach는 순서 보장 안 됨
    });
```

### ✅ 개선된 코드
```java
List<Integer> squaredNumbers = numbers.parallelStream()
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

[forEach 방식] 실행 시간: 56ms
[collect 방식] 실행 시간: 55ms  (더 빠름!)
```

---

## 🎯 사례 2: ParallelStream에서 Thread-Safe 컬렉션 사용

### 📂 파일: `StreamAPI3.java`

### ❌ 비효율적인 코드 (주석 처리됨)
```java
List<Integer> results = new ArrayList<>();
numbers.parallelStream().forEach(number -> {
    results.add(number * 2);  // ⚠️ Thread-Safe 하지 않음!
});
```

### ✅ 개선된 코드
```java
List<Integer> results2 = new CopyOnWriteArrayList<>();
numbers.parallelStream().forEach(number -> {
    results2.add(number * 2);  // ✅ Thread-Safe!
});
```
```
실행시간: 2992700ns
실행시간: 804000ns
```
### 💡 왜 개선되었나?

1. **ArrayList의 문제점**
   - 멀티 스레드 환경에서 동시에 `add()` 호출 시 **Race Condition** 발생
   - 데이터 손실 또는 `ArrayIndexOutOfBoundsException` 발생 가능
   - 예상치 못한 결과 발생

2. **CopyOnWriteArrayList의 장점**
   - 쓰기 작업 시 내부 배열을 복사하여 Thread-Safe 보장
   - 읽기 작업은 락 없이 수행 가능
   - 병렬 처리 환경에서 안전하게 사용 가능

### ⚠️ 주의사항
- `CopyOnWriteArrayList`는 쓰기가 적고 읽기가 많을 때 유리
- **더 나은 방법**: `collect(Collectors.toList())` 사용 권장!

---

## 🎯 사례 3: Stream 체이닝 최적화

### 📂 파일: `OverusingTest.java`

### ❌ 비효율적인 코드 (test1)
```java
List<String> result = names.stream()
    .filter(name -> name.startsWith("A"))      // 첫 번째 필터
    .filter(name -> name.length() > 3)         // 두 번째 필터
    .map(String::toUpperCase)                  // 첫 번째 변환
    .map(name -> name + " is a name")          // 두 번째 변환
    .toList();
```

### ✅ 개선된 코드 (test2)
```java
List<String> result = names.stream()
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
걸린 시간(나노초): 17741400 ns

[test2 - 체이닝 최적화]
결과: [ALICE is a name]
걸린 시간(나노초): 1140400 ns  (더 빠름!)
```

---

## 📋 전체 성능 비교 및 병렬 처리의 스케일링 효과

### 📂 파일: `case03_compare.java`

이 코드는 **작은 데이터셋**과 **큰 데이터셋**에서 병렬 스트림의 성능을 비교합니다.

### 🔬 실험 코드

#### ① 작은 데이터셋 (5개) - 비효율적인 예시
```java
List<Integer> smallNumbers = Arrays.asList(1, 2, 3, 4, 5);
long startTime1 = System.nanoTime();

smallNumbers.parallelStream()
    .map(n -> {
        System.out.println(
            Thread.currentThread().getName() + " processing: " + n
        );
        return n * n;
    })
    .forEach(System.out::println);

long endTime1 = System.nanoTime();
System.out.println("[비효율 코드 실행 시간(ns)] : " + (endTime1 - startTime1));
```

#### ② 큰 데이터셋 (1,000,000개) - 효율적인 예시
```java
List<Integer> largeNumbers = IntStream.rangeClosed(1, 1_000_000)
    .boxed()
    .collect(Collectors.toList());

long startTime2 = System.nanoTime();

List<Integer> squaredNumbers = largeNumbers.parallelStream()
    .map(n -> n * n)
    .collect(Collectors.toList());

long endTime2 = System.nanoTime();
System.out.println("First 10 squared numbers: " + squaredNumbers.subList(0, 10));
System.out.println("[개선 코드 실행 시간(ns)] : " + (endTime2 - startTime2));
```

### 💡 핵심 발견: 병렬 처리의 스케일링 효과

#### 1️⃣ 작은 데이터셋에서 parallelStream + println
**문제점**:
- 데이터가 5개밖에 없는데 여러 스레드를 생성하는 오버헤드
- `System.out.println()`은 내부적으로 **synchronized** 처리됨
- 각 스레드가 출력을 위해 락(lock)을 기다리며 대기
- 병렬 처리의 이점 < 스레드 관리 비용

**실행 결과 예시**:
```
ForkJoinPool.commonPool-worker-1 processing: 2
ForkJoinPool.commonPool-worker-2 processing: 3
main processing: 1
... (스레드가 경쟁하며 출력)
[비효율 코드 실행 시간(ns)] : 매우 느림 37,534,500 (수백만 ns)
```

#### 2️⃣ 큰 데이터셋에서 parallelStream + collect
**장점**:
- **데이터가 커질수록 병렬 처리의 진가 발휘!**
- 1,000,000개의 데이터를 여러 스레드가 분담하여 처리
- `collect()`는 내부적으로 효율적인 병합 전략 사용
- [개선 코드 실행 시간(ns)] : 99,108,600 (수백만 ns)
- **중요**: 데이터가 100만 개로 늘어나도 실행 시간이 100만 배 늘지 않음!

**스케일링 효과**:
```
데이터 1개 → 시간 X
데이터 100개 → 시간 약 10X (100배 증가하지 않음)
데이터 1,000,000개 → 시간 약 1000X (100만배 증가하지 않음!)
```

### 🎯 왜 시간이 선형적으로 늘어나지 않는가?

1. **병렬 처리의 작업 분할**
   ```
   전체 데이터 1,000,000개를 8개 코어가 나누어 처리
   → 각 코어는 약 125,000개씩만 처리
   → 이론적으로 8배 빠름 (실제로는 오버헤드로 5-6배 정도)
   ```

2. **효율적인 collect() 연산**
   - 각 스레드가 독립적으로 부분 결과를 생성
   - 최종 단계에서만 병합 수행
   - Thread-Safe한 방식으로 결과 수집

3. **CPU 캐시 활용**
   - 대량 데이터 처리 시 CPU 캐시 효율 증가
   - 예측 가능한 메모리 접근 패턴

### 📊 실험 조건 정리
| 구분 | 데이터 크기 | 연산 | 특징 |
|------|------------|------|------|
| **비효율적 코드** | 5개 | parallelStream + println | 스레드 오버헤드 > 성능 향상 |
| **개선 코드** | 1,000,000개 | parallelStream + collect | 병렬 처리의 이점 극대화 |

---

## ✨ 결론 및 Best Practices

### 1️⃣ ParallelStream 사용 시
- ✅ `collect(Collectors.toList())`를 사용하여 결과 수집
- ❌ `forEach`로 외부 컬렉션에 직접 추가하지 말 것

### 2️⃣ Thread-Safety
- ✅ 병렬 스트림에서는 Thread-Safe 컬렉션 사용
- ✅ 가능하면 `collect()`를 사용하여 안전하게 수집

### 3️⃣ Stream 체이닝
- ✅ 가능한 한 중간 연산을 통합
- ✅ 필터 조건은 `&&`로 결합
- ✅ 맵 변환은 하나의 람다로 처리

### 4️⃣ 성능 측정
- `System.nanoTime()`을 활용한 정확한 성능 측정
- 작은 데이터와 큰 데이터에서 모두 테스트

---

## 🙏 감사합니다!

**Q&A 시간** 💬
