package cz.muni.fi.IA158.robot.tasks.runnable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class EV3LineCarRunnable {

    public static void main(String[] args)
    {
    	BlockingQueue<Job> queueDist = new ArrayBlockingQueue<>(10);
    	BlockingQueue<Job> queueSteer = new ArrayBlockingQueue<>(10);
    	
    	SchedulerRunnable scheduler = new SchedulerRunnable(queueDist, queueSteer);
    	new Thread(scheduler).start();
    	
    	SteeringRunnable steering = new SteeringRunnable(queueSteer);
    	new Thread(steering).start();
    	DistanceCheckRunnable distance = new DistanceCheckRunnable(queueDist);
    	new Thread(distance).start();
    	
    	//long millis = System.currentTimeMillis(); // number of milliseconds from start of the epoch
    	
    }
}

