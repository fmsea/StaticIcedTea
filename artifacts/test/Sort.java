package test;

public class Sort {
	
	public void main(String[] args){
		
	}
	
	static void selectSort(int[] a, int N){
		for (int j=0; j<N-1;j++){
			int min=j;
			for (int i=j+1; i < N ; i++)
				if (a[min] > a[i]) min = i;
			int t = a[j]; a[j]=a[min]; a[min]=t;
		}
		for (int j=0; j<N-1; j++)
			assert a[j] <= a[j+1];
	}
	
	static void bubbleSort(int[] a, int N){
		for (int j=0; j<N-1; j++)
			for (int i=0; i<N-j-1; i++)
				if (a[i] > a[i+1]) {
					int t = a[i];
					a[i] = a[i+1];
					a[i+1] = t;
				}
		for (int j=0; j<N-1; j++)
			assert a[j] <= a[j+1];
	}

}
