package step06.streamapi;

import java.util.Arrays;
import java.util.List;

import model.domain.People;

public class StreamAPI1 {

	public static void main(String[] args) {

		List<String> datas = Arrays.asList("a", "b", "c", "d", "e");
		System.out.println(datas);
		System.out.println(datas.get(0).charAt(0));
		
		datas.forEach(v->System.out.println(v));
		datas.forEach(System.out::println);
		
		System.out.println("*** step01 : c 데이터만 출력");
		datas.forEach(v -> {
			if(v.equals("c")) {
				System.out.println(v);
			}
		});
		
		System.out.println("*** step02 : c 데이터(equals())만 출력 + Stream API");
		datas.stream().filter(v -> v.equals("c")).forEach(System.out::println);
		datas.stream().filter(v -> !v.equals("c")).forEach(System.out::println);
		
		//? People의 이름들만 출력
		List<People> datas2 = Arrays.asList(new People("연아", 30), new People("재석", 50));
		
		datas2.stream().filter(p->p.getName().equals("연아")).forEach(v->System.out.println(v.getName()));
		
	}

}
