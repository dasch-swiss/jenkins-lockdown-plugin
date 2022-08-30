package swiss.dasch.plugins.lockdown;

import java.util.List;

import hudson.ExtensionList;
import hudson.ExtensionPoint;

public interface LockdownMessageListener extends ExtensionPoint {

	public void onLockdownMessageChanged();

	public static List<LockdownMessageListener> all() {
		return ExtensionList.lookup(LockdownMessageListener.class);
	}

}
