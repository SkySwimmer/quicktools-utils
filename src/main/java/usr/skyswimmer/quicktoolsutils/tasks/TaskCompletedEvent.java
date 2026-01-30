package usr.skyswimmer.quicktoolsutils.tasks;

import usr.skyswimmer.quicktoolsutils.events.EventObject;

/**
 * 
 * Async task completion event
 * 
 * @param <T> Task result type
 * 
 * @author Sky Swimmer
 * 
 */
public class TaskCompletedEvent<T> extends EventObject {

	private T result;

	public TaskCompletedEvent(T result) {
		this.result = result;
	}

	/**
	 * Retrieves the task result
	 * 
	 * @return Task result object
	 */
	public T getResult() {
		return result;
	}

}
