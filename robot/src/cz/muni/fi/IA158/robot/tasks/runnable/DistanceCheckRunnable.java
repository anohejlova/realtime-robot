package cz.muni.fi.IA158.robot.tasks.runnable;

import java.util.concurrent.BlockingQueue;

public class DistanceCheckRunnable implements Runnable{
	
	private BlockingQueue<Job> queueDist;
	long releaseDeadlineDiff = 200;
	
	public DistanceCheckRunnable(BlockingQueue<Job> queueDist) {
		this.queueDist = queueDist;
	}

	@Override
	public void run() {
		while (true) {
			//distance job
			long now = System.currentTimeMillis(); // number of milliseconds from start of the epoch
			Job release = new Job(now, now + releaseDeadlineDiff);
			queueDist.add(release);
			System.err.println("distance");
			synchronized(this) {
				try {
					this.wait();
				} catch (InterruptedException e) {
					System.err.println("InterruptedException steering wait");
					e.printStackTrace();
				}
			}
		}
	}

}
