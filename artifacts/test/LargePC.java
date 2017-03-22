package test;

import java.io.IOException;

public class LargePC {
		

		int field1;

		int field2;
		
//		public int foo(char[] ca) {  
//		    int cnt = 0;
//		    char a = 'a';
//		    char b = 'b';
//		    ca[0] = a;
//		    
//		    if (ca[0] == a) {
//		        cnt++;
//		        for (char c: ca) {
//		            if (c == a) {
//		                cnt++;
//		            } else {
//		            	cnt--;
//		            }
//		        }
//		    } else if (ca[0] == b) {
//		        cnt--;
//		        for (char c: ca) {
//		            //if (c == 'b') {
//		        	if (c == ca[0] && c == ca[1]) {
//		                cnt--;
//		            }
//		        }
//		    }
//		    
//		    return cnt;
//		}
//		
//		public int goo(int x, int y) {
//			System.out.println(x + " " + y);
//			int z = 0;
//			if (2 < x) {
//				if ( x != 2)
//				System.out.println(x);
//			} 
//			/*
//			int z = x + y;
//	        if (z > 0) {
//	            z = 1;
//	        } else {
//	            z = z - 4;
//	        	z = goo2(y, x);
//	        	if (z < x) {
//	        		x++;
//	        		if (x > this.field1) {
//	        			x--;
//	        		}
//	        	}
//	        }
//	        z = 2 * z;
//	        */
//			return z;
//		}
		
		public static String stringTest(String origS){
			String curS = "";
			if(origS.contains("aa")){
				curS = "_" + origS.indexOf("aa");
			} else {
			    curS += origS+"_";	
			}
			return curS;
		}
		
		public int goo2(int x, int y) {
			int z = x;
			if(z > y ) {
				z = 2*x;
			} else if (z == 0){
				z = x - y;
			}

			while(z > 5) {
				z--;
			}

			z = -z;
			if(z == 0){
				System.out.println("assert");
			}
			return z;
		}
		
		public int soogood(int x, int y, int z){
			int ret = 0;
			if(x > y){ //1
				ret++;
				x = x/2;
				if(ret !=z ){//2
					z = y*x;
					z--;
				} else {
					z = y/x;
					z++;
				}
				ret = ret + z;
			} 
			else {
				if(x < z){//3
					ret --;
					while(y > ret){//4
						ret = x - 8*z +y;
					} 
//					else {
//						ret = x + 8*z -y;
//					}
				} else {
					x = x -1;
					y = y -1;
					z = 2*x - 3*y;
					ret = z - 2;
				}
			}
			
			if(ret == 0){
				ret = 1;
			} else {
				ret = -ret;
			}
			return ret;
		}
		
public int loopy(int x, int y){
	while(x > y){
		y = y*2;
		x--;
	}
	return x;
}
		public static void main(String[] args) throws IOException {
			LargePC t = new LargePC();
			//char[] cs = Files.getFileContents(args[0]).toCharArray();
			//String[] argsSplit = args[0].split(" ");
			//char[] cs = argsSplit[0].toCharArray();
			//t.field1 = Integer.parseInt(argsSplit[1]);
			//t.foo(cs);
			t.goo2(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
			//DebugFuzz.fuzzPC("\nTestClass1.testMethod Path Condition: ");
			stringTest("haa");
		}
	
}
