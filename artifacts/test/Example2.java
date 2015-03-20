package test;

public class Example2 {

	static int example(int x1, int y1, int x2){
		//x1 = -170000000;
//		if (x1 == 0){
//			x1++; 
//		} else {
//			x1--;
//		}
		
		

		while(x1 >= 2) {
			while (y1 >=5){
				//y1++;
				while (x2 < -1){ 
				if (x1 == 0) {
					System.out.println("assert");
				}
				}
			}
			//x1 = y1 - x1;
		}
		
		return y1;

	}

	public static void main(String[] args){
		int x = Integer.valueOf(args[0]);
		int y = Integer.valueOf(args[1]);
		
		System.out.println(example(x,y, x+y));
	}

}
