package usr.skyswimmer.quicktoolsutils.tasks;

import usr.skyswimmer.quicktoolsutils.events.EventObject;

/**
 * 
 * Async task error event
 * 
 * @author Sky Swimmer
 * 
 */
public class TaskErroredEvent extends EventObject {

	private Exception error;

	public TaskErroredEvent(Exception error) {
		this.error = error;
	}

	/**
	 * Retrieves the task exception
	 * 
	 * @return Task exception object
	 */
	public Exception getError() {
		return error;
	}

}
