package cz.muni.fi.IA158.robot.tasks.runnable;

/**
 * Class representing one job of any task.
 */
public class Job {
	//attributes of each job
    private long releaseTime;
    private long deadline;
    
    /**
     * Constructor of the Job
     * 
     * Sets the release time and deadline of the job
     * 
     * @param releaseTime Release time of the job in milliseconds from start of the epoch
     * @param deadline Deadline of the job in milliseconds from start of the epoch
     */
    public Job(long releaseTime, long deadline){
        this.releaseTime=releaseTime;
        this.deadline=deadline;
    }
    
    /**
     * Gets the release time of the job
     * @return release time in milliseconds from start of the epoch
     */
    public long getReleaseTime() {
    	return releaseTime;
    }
    
    /**
     * Gets the deadline of the job
     * @return deadline in milliseconds from start of the epoch
     */
    public long getDeadline() {
    	return deadline;
    }

}