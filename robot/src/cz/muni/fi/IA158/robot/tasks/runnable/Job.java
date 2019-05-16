package cz.muni.fi.IA158.robot.tasks.runnable;

/**
 * Class representing jobs for each task *
 */
public class Job {
	//attributes of each job
    private long releaseTime;
    private long deadline;
    
    //constructor for new job
    public Job(long releaseTime, long deadline){
        this.releaseTime=releaseTime;
        this.deadline=deadline;
    }
    
    public long getReleaseTime() {
    	return releaseTime;
    }
    
    public long getDeadline() {
    	return deadline;
    }

}