package tfc.smallerunits.utils.async;

import org.apache.logging.log4j.util.TriConsumer;

public class AsyncDispatcher {
	private final ThreadGroup group;
	private int iterCount;
	private TriConsumer<Integer, Integer, AsyncDispatcher> function;
	private int maxThreadCount;
	//	private boolean[] threadReadinessStates;
	private Thread[] threads;
	
	public AsyncDispatcher(String name) {
		this.group = new ThreadGroup(name);
	}
	
	// set the number of iterations to go through
	public AsyncDispatcher setIterations(int count) {
		iterCount = count;
		return this;
	}
	
	// resize the thread pool
	public AsyncDispatcher resize(int maxThreads) {
		maxThreadCount = maxThreads;
		threads = new Thread[maxThreads];
//		Arrays.fill(threadReadinessStates, true);
		return this;
	}
	
	// set the function to be run by the dispatcher for each iteration
	public AsyncDispatcher updateFunction(TriConsumer<Integer, Integer, AsyncDispatcher> function) {
		this.function = function;
		return this;
	}
	
	public void unlockThread(Thread thread) {
//		threads[index] = true;
	}
	
	// awaits a specific thread to have finished
	public AsyncDispatcher await(int threadId) throws InterruptedException {
//		while (!threadReadinessStates[threadId]) {
//			try {
//				Thread.sleep(1);
//			} catch (Throwable ignored) {
//			}
//		}
		if (threads[threadId] != null && threads[threadId].isAlive()) threads[threadId].join();
		return this;
	}
	
	// awaits all of the threads to have finished
	public AsyncDispatcher await() throws InterruptedException {
//		for (boolean threadReadinessState : threadReadinessStates) {
//			while (threadReadinessState) {
//				try {
//					Thread.sleep(1);
//				} catch (Throwable ignored) {
//				}
//			}
//		}
//		for (int i = 0; i < threadReadinessStates.length; i++) await(i);
		for (int i = 0; i < threads.length; i++) await(i);
		return this;
	}
	
	// execute the threads
	public AsyncDispatcher dispatch() throws InterruptedException {
		for (int i = 0; i < iterCount; i++) {
			final int trueIndx = i;
			int indx = i % maxThreadCount;

//			// await the first thread to be open
//			if (indx == 0) await(maxThreadCount - 1);
			
			// await current thread to be free
			await(indx);
			// lock thread
//			threadReadinessStates[indx] = false;
			// thread as an array so it can reference itself
			// TODO: use pausing to reduce overhead
			Thread[] td = new Thread[1];
			td[0] = new Thread(group, () -> {
				function.accept(indx, trueIndx, this);
//				this.unlockThread(indx); // unlock thread
//				td[0].stop(); // make sure the thread actually stops
			});
			td[0].setName(i + "-" + group.activeCount());
			threads[indx] = td[0];
			td[0].setDaemon(true);
			// start the thread, duh
			td[0].start();
		}
		return this;
	}
	
	// check if the specified thread has finished
	public boolean isThreadReady(int index) {
//		return threadReadinessStates[index];
		return threads[index].isAlive();
	}
	
	// get the max number of threads the dispatcher can currently support
	public int maxThreads() {
		return maxThreadCount;
	}
	
	public int size() {
		return maxThreadCount;
	}
}
