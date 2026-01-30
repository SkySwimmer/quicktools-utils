package usr.skyswimmer.githubwebhooks.api.util.events.impl.asm;

import usr.skyswimmer.githubwebhooks.api.util.events.EventObject;
import usr.skyswimmer.githubwebhooks.api.util.events.IEventReceiver;

public interface IEventDispatcher {

	public void dispatch(IEventReceiver receiver, EventObject event);

}
