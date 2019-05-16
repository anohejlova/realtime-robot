package cz.muni.fi.IA158.robot.tasks.runnable;

/**
 * Class is used as a shared variable for synchronizing threads.
 * The one stored value represents whether the thread should run or sleep.
 */
public class Suspender {

	private boolean suspended = false;
	
	public Suspender() {
		
	}
	
	public boolean getSus() {
		return suspended;
	}
		
	public void setSus(boolean value) {
		suspended = value;
	}
	
	
	
}
