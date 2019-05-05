package cz.muni.fi.IA158.robot.tasks.runnable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class EV3LineCarRunnable {

    public static void main(String[] args)
    {
    	BlockingQueue<Job> queueDist = new ArrayBlockingQueue<>(10);
    	BlockingQueue<Job> queueSteer = new ArrayBlockingQueue<>(10);
    	
    	
    	
    	SteeringRunnable steering = new SteeringRunnable(queueSteer);
    	DistanceCheckRunnable distance = new DistanceCheckRunnable(queueDist);
    	Thread steerThread = new Thread(steering);
    	Thread distThread = new Thread(distance);
    	steerThread.start();
    	distThread.start();
    	new Thread(distance).start();
    	
    	Job distJob = null;
    	Job steerJob = null;
    	
    	while (true) {  //should be replaced by sensible condition
    		
	    	if (distJob == null) {
	    		
				distJob = queueDist.poll();
			}
	    	if (steerJob == null) {
	    		
	    		steerJob = queueSteer.poll();
			}
			if ((distJob != null) && (steerJob != null)) {
				
				if (distJob.getDeadline() < steerJob.getDeadline()) {
					distThread.run();
					distJob = null;
				} else {
					steerThread.run();
					steerJob = null;
				}
			} else if (steerJob != null) {
				steerThread.run();
				distJob = null;
			} else if (distJob != null) {
				distThread.run();
				steerJob = null;
			}
    	}
		
    	
    	//long millis = System.currentTimeMillis(); // number of milliseconds from start of the epoch
    	
    }
}

