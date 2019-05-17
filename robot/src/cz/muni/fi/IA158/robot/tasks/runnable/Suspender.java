package cz.muni.fi.IA158.robot.tasks.runnable;

/**
 * Class Suspender is used as a shared variable for synchronizing threads.
 * The one stored value represents whether the thread should run or sleep.
 * 
 */
public class Suspender {

	private boolean suspended = false;
	/**
	 * Constructor of class Suspender
	 */
	public Suspender() {
		
	}
	
	/**
	 * Method getSus gets the state of suspender
	 * @return if the thread should sleep now
	 */
	public boolean getSus() {
		return suspended;
	}
		
	/**
	 * Method setSus sets the state of suspender - if the thread should run or sleep
	 * @param value true if the thread should sleep, false otherwise
	 */
	public void setSus(boolean value) {
		suspended = value;
	}
	
	
	
}
