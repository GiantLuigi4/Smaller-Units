package tfc.smallerunits.utils.threading;

public class ReusableThread {
	private final Thread td;
	private Runnable action;
	private boolean isInUse;
	private boolean hasStarted = false;
	
	public ReusableThread(Runnable action) {
		this.action = action;
		td = new Thread(() -> {
			while (true) {
				this.action.run();
				isInUse = false;
				ReusableThread.this.td.suspend();
			}
		});
		td.setDaemon(true);
	}
	
	public void setAction(Runnable action) {
		this.action = action;
	}
	
	public void start() {
		while (isInUse) {
		}
		isInUse = true;
		if (!hasStarted) {
			td.start();
			hasStarted = true;
		} else td.resume();
	}
	
	public boolean isInUse() {
		return isInUse;
	}
}
