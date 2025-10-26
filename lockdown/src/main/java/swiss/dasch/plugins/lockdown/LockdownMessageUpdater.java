package swiss.dasch.plugins.lockdown;

import hudson.Extension;
import hudson.model.PeriodicWork;
import io.jenkins.cli.shaded.org.apache.commons.lang.StringUtils;

@Extension
public class LockdownMessageUpdater extends PeriodicWork {

	@Override
	public long getRecurrencePeriod() {
		return 1000;
	}

	@Override
	protected void doRun() throws Exception {
		LockdownManager manager = LockdownManager.get();

		boolean hasLockdowns = manager.hasLockdowns();

		boolean save = false;

		if (hasLockdowns) {
			String newLockdownMessage = manager.renderLockdownMessage();

			if (!StringUtils.equals(manager.getLockdownMessage(), newLockdownMessage)) {
				manager.setLockdownMessage(newLockdownMessage);

				LockdownMessageListener.all().forEach(l -> l.onLockdownMessageChanged());

				save = true;
			}
		} else if (!StringUtils.equals(manager.getLockdownMessage(), "")) {
			manager.setLockdownMessage("");

			LockdownMessageListener.all().forEach(l -> l.onLockdownMessageChanged());

			save = true;
		}

		if (save) {
			manager.save();
		}
	}

}
