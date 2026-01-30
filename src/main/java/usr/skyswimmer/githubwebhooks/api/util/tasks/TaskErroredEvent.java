package usr.skyswimmer.githubwebhooks.api.util.tasks;

import usr.skyswimmer.githubwebhooks.api.util.events.EventObject;

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
