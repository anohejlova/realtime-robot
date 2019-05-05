package cz.muni.fi.IA158.robot.tasks.runnable;
import java.util.concurrent.BlockingQueue;

public class SchedulerRunnable implements Runnable{
	BlockingQueue<Job> queueDist; 
	BlockingQueue<Job> queueSteer;
	
	public SchedulerRunnable(BlockingQueue<Job> queueDist, BlockingQueue<Job> queueSteer) {
		this.queueDist = queueDist;
		this.queueSteer = queueSteer;
	}
	
	@Override
	public void run() {
		Job distJob, steerJob;
		distJob = queueDist.poll();
		steerJob = queueSteer.poll();
		if ((distJob != null) && (steerJob != null)) {
			if (distJob.getDeadline() < steerJob.getDeadline()) {
				;
			} else {
				;
			}
		}
	}

}
