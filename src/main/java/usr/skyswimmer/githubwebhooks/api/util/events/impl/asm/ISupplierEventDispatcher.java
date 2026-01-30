package usr.skyswimmer.githubwebhooks.api.util.events.impl.asm;

import usr.skyswimmer.githubwebhooks.api.util.events.IEventReceiver;
import usr.skyswimmer.githubwebhooks.api.util.events.SupplierEventObject;

public interface ISupplierEventDispatcher {

	public Object dispatch(IEventReceiver receiver, SupplierEventObject<?> event);

}
