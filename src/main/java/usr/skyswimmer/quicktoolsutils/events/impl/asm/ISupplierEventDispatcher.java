package usr.skyswimmer.quicktoolsutils.events.impl.asm;

import usr.skyswimmer.quicktoolsutils.events.IEventReceiver;
import usr.skyswimmer.quicktoolsutils.events.SupplierEventObject;

public interface ISupplierEventDispatcher {

	public Object dispatch(IEventReceiver receiver, SupplierEventObject<?> event);

}
