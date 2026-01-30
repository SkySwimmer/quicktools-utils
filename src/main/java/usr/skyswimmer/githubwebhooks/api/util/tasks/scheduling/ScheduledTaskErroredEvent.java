package usr.skyswimmer.githubwebhooks.api.util.tasks.scheduling;

import usr.skyswimmer.githubwebhooks.api.util.events.EventObject;

/**
 * 
 * Scheduled task error event
 * 
 * @author Sky Swimmer
 * 
 */
public class ScheduledTaskErroredEvent extends EventObject {

	private Exception error;

	public ScheduledTaskErroredEvent(Exception error) {
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
