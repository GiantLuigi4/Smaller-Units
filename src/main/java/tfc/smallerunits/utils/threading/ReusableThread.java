package tfc.smallerunits.utils.threading;

public class ReusableThread {
	private Runnable action;
	private Thread td = null;
	private boolean isRunning = false;
	private boolean isStopping = false;
	
	public ReusableThread(Runnable action) {
		this.action = action;
		td = new Thread(() -> {
			while (true) {
				this.action.run();
				try {
					isRunning = false;
					while (!isRunning) {
						if (isStopping) return;
						// wait on an interrupted exception
						// reason: td.suspend/td.resume is likely to deadlock
						// waiting on an interrupted exception works 100% of the time
						Thread.sleep(Long.MAX_VALUE);
					}
				} catch (Exception ignored) {
					if (isStopping) return;
				}
			}
		});
		td.setDaemon(true);
	}
	
	public boolean isRunning() {
		return isRunning;
	}
	
	public void setAction(Runnable action) {
		this.action = action;
	}
	
	public void start() {
		await();
		isRunning = true;
		if (!td.isAlive()) td.start();
			// force the thread out of it's sleep loop
		else td.interrupt();
	}
	
	public void await() {
		while (isRunning) {
			try {
				Thread.sleep(1);
			} catch (Throwable ignored) {
			}
		}
	}
	
	public void forceKill() {
		try {
			isStopping = true;
			td.interrupt();
			td.stop();
		} catch (Throwable ignored) {
		}
	}
	
	public void kill() {
		try {
			await();
			// allows the thread to exit gracefully
			isStopping = true;
			td.interrupt();
		} catch (Throwable err) {
			err.printStackTrace();
		}
	}
	
	public boolean isInUse() {
		return isRunning;
	}
}
