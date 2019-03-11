package util;

public class Timer {
	protected long startTime;

	public Timer() {
		this.startTime = 0;
	}
	
	public void start() {
		this.startTime = System.currentTimeMillis();
	}
	
	public long stop() {
		return System.currentTimeMillis() - startTime;
	}
}
