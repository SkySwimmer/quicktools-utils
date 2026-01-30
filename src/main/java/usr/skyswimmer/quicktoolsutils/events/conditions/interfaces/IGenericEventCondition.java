package usr.skyswimmer.quicktoolsutils.events.conditions.interfaces;

import java.lang.reflect.Method;

import usr.skyswimmer.quicktoolsutils.events.EventObject;
import usr.skyswimmer.quicktoolsutils.events.IEventReceiver;

public interface IGenericEventCondition {

	/**
	 * Indicates if the condition supports static event listeners
	 * 
	 * @return True if static listeners are supported, false otherwise
	 */
	public default boolean supportsStatic() {
		return false;
	}

	/**
	 * Checks if the event matches this condition
	 * 
	 * @param receiverType Receiver type
	 * @param listener     Event listener
	 * @param event        Event object
	 * @return True if valid, false otherwise
	 */
	public boolean match(IEventReceiver receiverType, Method listener, EventObject event);

}
