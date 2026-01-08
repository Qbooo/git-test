package step06.streamapi;

import java.util.Arrays;
import java.util.List;

import model.domain.People;

public class StreamAPI2 {

	public static void main(String[] args) {
		List<String> datas = Arrays.asList("1", "2", "3", "4", "15", "3", "3");
		datas.stream().forEach(v -> System.out.println(v + 1));

		int all =datas.stream().mapToInt(Integer::parseInt).sum();//forEach(v -> System.out.println(v + 1));
		
		double all2 = datas.stream().mapToDouble(Double::parseDouble).sum();
		System.out.println(all + " " + all2);
		
		/* Optional - 값을 보유하는 컨테이너
		 * OptionalInt - int값 보유 컨테이너
		 * 
		 *
		 */
		// datas의 데이터 개수 출력 + stream().mapToInt()... 사용하시면서 int 데이터 개수만 출력
		System.out.println(datas.stream().mapToInt(Integer::parseInt).count());
		System.out.println(datas.stream().mapToInt(Integer::parseInt).max());
		
		int data1 = datas.stream().mapToInt(Integer::parseInt).max().getAsInt();
		System.out.println(data1);
		
		//datas가 보유한 문자열을 int로 변환해서 3값에 한해서만 filtering 후 총합 출력 하기
		int sumData = datas.stream().mapToInt(Integer::parseInt).filter(v->v == 3).sum();
		System.out.println("3값들의 총 합 : " + sumData);
		
		List<People> datas2 = Arrays.asList(new People("연이", 30), new People("재석", 50));
		
		//? 나이 값만 구하기
		//int ageSum = datas2.stream().mapToInt(p -> p.getAge()).sum();
		int ageSum = datas2.stream().mapToInt(People::getAge).sum();
		System.out.println("나이 합 : " + ageSum);
		/* Stream<T> filter(?) :  api에서 확인되는 api 이름
		 * 실제 구현체 즉 생성된 객체명은 ReferencePipeline
		 * - Stream 상속받는 하위 클래스
		 * 
		 * JDK자체적으로 interface(미완성 메소드) 설계
		 * 동적으로 실행시 미완성 로직들을 상속 ㅂ다아서 모든 메소드들 재정의 하는 하위 클래스를 생성
		 * 객체 생성이 가능(미완성 메소드 하나라도 보유한 interface는 객체 생성 불가)
		 * 
		 */
		//?나이가 40이상인 People 출력
		
		System.out.println(datas2.stream().filter(p->p.getAge() > 40));
		System.out.println(datas2.stream().filter(p->p.getAge() > 40).toString());
		
		//Optional[People(name=재석, age =50)]
		System.out.println(datas2.stream().filter(p->p.getAge() > 40).findFirst());
		
		
	}

}
