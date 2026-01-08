package step06.streamapi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class StreamAPI3 {
	
	static void myFunction(Persu p) {
		long startNs = System.nanoTime();
        p.run();
        long endNs = System.nanoTime();
        System.out.println("실행시간: " + (endNs - startNs) + "ns");
	}

	public static void main(String[] args) {

		List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
		List<Integer> results = new ArrayList<>();
		
		//
		myFunction(()->numbers.parallelStream().forEach(number -> {
			results.add(number * 2);
		}));
		

		
		List<Integer> results2 = new CopyOnWriteArrayList<>();
		
		myFunction(()->numbers.parallelStream().forEach(number -> {
			results2.add(number * 2);
		}));
		
	}

}
