package disjoint.analysis.concurrent;

public class BoundedBuffer {
	static int BUFFER_SIZE = 1;
	static int N_PRODUCERS = 4;
	static int N_CONSUMERS = 4;
	
	static Object DATA = "fortytwo";
	
	//--the boundend buffer implemetation
	protected Object[] buf;
	protected int in;
	protected int out;
	protected int count;
	protected int size;
	
	public BoundedBuffer(int size){
		this.size = size;
		buf = new Object[size];
		in = 0;
		out = 0;
		count = 0;
	}
	
	public synchronized void put(Object o) throws InterruptedException{
		while(count == size){
			wait();
		}
		buf[in] = o;
		++count;
		in = (in +1)%size;
		notifyAll();// if this is not notifyAll() we might notify the wrong waiter
	}
	
	public synchronized Object get() throws InterruptedException{
		while(count == 0){
			wait();
		}
		Object o = buf[out];
		buf[out] = null;
		--count;
		out = (out+1) %size;
		notifyAll(); // if this is not notifyAll() we might notify a wrong waiter thread.
		return o;
	}
	
	//inner classes for the thread type
	static class Producer extends Thread{
		static int nProducers = 1;
		BoundedBuffer buf;
		Producer(BoundedBuffer b){
			buf = b;
			setName("P"+ nProducers++);
		}
		@Override
		public void run(){
			try{
				while(true){
					buf.put(DATA);
					System.out.println(Thread.currentThread().getName() + " " + DATA);
				}
			} catch (InterruptedException e){}
		}
	}
	
    static class Consumer extends Thread{
    	static int nConsumers = 1;
    	BoundedBuffer buf;
    	
    	Consumer(BoundedBuffer b){
    		buf = b;
    		setName("C"+nConsumers++);
    	}
    	
    	@Override
    	public void run(){
    		try{
    			while(true){
    				Object tmp = buf.get();
    				System.out.println(Thread.currentThread().getName() + " " + tmp);
    			}
    		} catch (InterruptedException e){}
    	}
    }
	
//the test driver
    public static void main(String[] args){
    	BoundedBuffer buf = new BoundedBuffer(BUFFER_SIZE);
    	
    	for(int i = 0; i < N_PRODUCERS; i++){
    		new Producer(buf).start();
    	}
    	
    	for(int i = 0; i <  N_CONSUMERS; i++){
    		new Consumer(buf).start();
    	}
    }
}
