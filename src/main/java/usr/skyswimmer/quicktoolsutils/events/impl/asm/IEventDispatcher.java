package usr.skyswimmer.quicktoolsutils.events.impl.asm;

import usr.skyswimmer.quicktoolsutils.events.EventObject;
import usr.skyswimmer.quicktoolsutils.events.IEventReceiver;

public interface IEventDispatcher {

	public void dispatch(IEventReceiver receiver, EventObject event);

}
