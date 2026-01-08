package step06.streamapi;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StreamAPI4 {

    public static void main(String[] args) {
        // 같은 데이터로 테스트
        List<Integer> numbers = IntStream.rangeClosed(1, 1_000_000)
                .boxed()
                .collect(Collectors.toList());
        
        System.out.println("=== 데이터 크기: " + numbers.size() + "개 ===\n");
        
        // ===============================
        // ① 비효율적인 코드 (println 포함)
        // ===============================
        long startTime1 = System.nanoTime();
        numbers.parallelStream()
                .map(n -> {
                    // System.out.println 제거 (엄청 느림)
                    return n * n;
                })
                .forEach(result -> {
                    // forEach는 순서 보장 안 됨
                });
        long endTime1 = System.nanoTime();
        System.out.println("[forEach 방식] 실행 시간: " + 
                          (endTime1 - startTime1) / 1_000_000 + "ms");
        
        // ===============================
        // ② 개선된 코드 (collect 사용)
        // ===============================
        long startTime2 = System.nanoTime();
        List<Integer> squaredNumbers = numbers.parallelStream()
                .map(n -> n * n)
                .collect(Collectors.toList());
        long endTime2 = System.nanoTime();
        System.out.println("[collect 방식] 실행 시간: " + 
                          (endTime2 - startTime2) / 1_000_000 + "ms");
        
        System.out.println("\n=== 결론 ===");
        System.out.println("✅ collect()가 더 빠르고 안전함");
    }
}