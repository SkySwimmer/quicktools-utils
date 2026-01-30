package usr.skyswimmer.quicktoolsutils.events.impl.asm;

import usr.skyswimmer.quicktoolsutils.events.SupplierEventObject;

public interface IStaticSupplierEventDispatcher {

	public Object dispatch(SupplierEventObject<?> event);

}
