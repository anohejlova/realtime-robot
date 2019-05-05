package cz.muni.fi.IA158.robot.tasks.runnable;

public class Job {
    private long releaseTime;
    private long deadline;
    
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