package swiss.dasch.plugins.lockdown;

import java.util.List;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Job;

public interface LockdownStateListener extends ExtensionPoint {

	public void onLockdownStateChanged(Job<?, ?> job, boolean lockedDown);

	public static List<LockdownStateListener> all() {
		return ExtensionList.lookup(LockdownStateListener.class);
	}

}
