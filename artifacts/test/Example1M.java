package test;

public class Example1M {

	static int example(int x1, int y1){
		int a = x1*5;

		if(a != 1){
			x1 = 1;
		}

		if (x1 == 6){
			while (x1 != 2 || y1 < 7){
				y1 = y1 + 2;
				while(y1 > 0) {
					y1--;
				}
				if(x1 == 0){
					System.out.println("assert");
				}
				x1=0;
			}	
		} else {
			y1 = 5;
		}
		x1++;
		return y1;

	}

	static int example_1(int x1, int y1){
		x1 = x1*2;
		x1 = x1 - y1;
		int z1 = -1;
		while (x1 != 2){
			y1 = y1 + 2;
			z1 = 3 * z1;
			while(y1 > 0) {
				y1 = y1 - 5*z1;
			}
			if(x1 == 0){
				System.out.println("assert");
				//System.exit(2);
			}
			x1++;
		}	 
		return y1;

	}

	static int example_2(int x1, int x2){
		int x = x1 + x2;
		boolean cond_1;
		if(x == 0){
			cond_1 = false;
		} else {
			cond_1 = true;
		}
		if(x1==x2 && x != 5 && !cond_1){
			System.out.println("assert");
			System.exit(2);
		}
		return x;
	}

	static int example_3(int x, int y){
		if (x == 0) {
			y = 3;
			x = y * 11;
		} else {
			y = 1;
		}
		y++;
		if (y > 0) {
			x = y;
			y = 22*x - 11;
		} else {
			x = y+1;
		}

		if(x == 0){
			System.out.println("assert");
			if(y>2){
				y++;
				x = y * x;
			} else {
				y--;
				x = y /2;
			}
		} else {
			x = 1; 
		}
		return x;
	}

	static int example_4(int y){
		int x = -50;
		while(x < 0){
			x = x + y;
			y = 2+x;
		}
		return y;
	}

	static int example_5(int x){
		int y = 1;
		x = y - 3;
		if(x == 0){
			y = x - y;
		} else {
			y = x + y;
		}
		y = x * y - 18;
		return y;
	}

	static int example_6(int z){
		int x = z + 1;
		int y = 2;
		z = x -y;
		return z;
	}

	static int example_7(int radius, int width){
		int ret = 0;
		if(radius < width){
			int j = 5;
			System.out.println(j);
		} else {
			for(int i=0; i < width; i++){
				ret +=i;
			}
		}
		return ret;
	}

	public static void main(String[] args){
		int x = Integer.valueOf(args[0]);
		int y = Integer.valueOf(args[1]);

		System.out.println(example_3(x,y));
	}

}
