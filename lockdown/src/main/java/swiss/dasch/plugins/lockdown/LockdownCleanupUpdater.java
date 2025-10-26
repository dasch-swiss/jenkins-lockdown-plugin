package swiss.dasch.plugins.lockdown;

import hudson.Extension;
import hudson.model.PeriodicWork;

@Extension
public class LockdownCleanupUpdater extends PeriodicWork {

	@Override
	public long getRecurrencePeriod() {
		return 10000;
	}

	@Override
	protected void doRun() throws Exception {
		LockdownManager.get().deleteStaleLockdownStates();
	}

}
