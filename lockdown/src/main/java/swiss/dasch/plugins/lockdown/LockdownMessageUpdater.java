package swiss.dasch.plugins.lockdown;

import hudson.Extension;
import hudson.model.PeriodicWork;
import io.jenkins.cli.shaded.org.apache.commons.lang.StringUtils;

@Extension
public class LockdownMessageUpdater extends PeriodicWork {

	private transient LockdownManager lockdownManager;

	public LockdownMessageUpdater() {
		this.lockdownManager = LockdownManager.get();
	}

	@Override
	public long getRecurrencePeriod() {
		return 1000;
	}

	@Override
	protected void doRun() throws Exception {
		boolean hasLockdowns = this.lockdownManager.hasLockdowns();

		boolean save = false;

		if (hasLockdowns) {
			String newLockdownMessage = this.lockdownManager.renderLockdownMessage();

			if (!StringUtils.equals(this.lockdownManager.getLockdownMessage(), newLockdownMessage)) {
				this.lockdownManager.setLockdownMessage(newLockdownMessage);

				LockdownMessageListener.all().forEach(l -> l.onLockdownMessageChanged());

				save = true;
			}
		} else if (!StringUtils.equals(this.lockdownManager.getLockdownMessage(), "")) {
			this.lockdownManager.setLockdownMessage("");

			LockdownMessageListener.all().forEach(l -> l.onLockdownMessageChanged());

			save = true;
		}

		if (save) {
			this.lockdownManager.save();
		}
	}

}
