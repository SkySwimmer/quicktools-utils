package usr.skyswimmer.githubwebhooks.api.util.events.impl.asm;

import usr.skyswimmer.githubwebhooks.api.util.events.SupplierEventObject;

public interface IStaticSupplierEventDispatcher {

	public Object dispatch(SupplierEventObject<?> event);

}
