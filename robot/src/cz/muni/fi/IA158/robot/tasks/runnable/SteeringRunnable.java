package cz.muni.fi.IA158.robot.tasks.runnable;

import java.util.concurrent.BlockingQueue;

public class SteeringRunnable implements Runnable{

	BlockingQueue<Job> queueSteer;
	long releaseDeadlineDiff = 100;
	
	public SteeringRunnable(BlockingQueue<Job> queueSteer) {
		this.queueSteer = queueSteer;
	}
	
	@Override
	public void run() {
		while (true) {
			//steering job
			long now = System.currentTimeMillis(); // number of milliseconds from start of the epoch
			Job release = new Job(now, now + releaseDeadlineDiff);
			queueSteer.add(release);
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}

}
