package swiss.dasch.plugins.lockdown;

import hudson.Extension;
import hudson.model.PeriodicWork;
import io.jenkins.cli.shaded.org.apache.commons.lang.StringUtils;
import jenkins.model.Jenkins;

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
		Jenkins jenkins = Jenkins.get();
		boolean hasLockdowns = this.lockdownManager.hasLockdowns();

		boolean save = false;

		if (hasLockdowns) {
			String newLockdownMessage = this.lockdownManager.renderLockdownMessage();

			if (this.lockdownManager.getUseSystemMessage()) {
				boolean shouldUpdateSystemMessage = !this.lockdownManager.getSystemMessageSet()
						|| !StringUtils.equals(jenkins.getSystemMessage(), newLockdownMessage);

				if (this.canUpdateSystemMessage() && shouldUpdateSystemMessage) {
					jenkins.setSystemMessage(newLockdownMessage);

					this.lockdownManager.setLastSystemMessage(newLockdownMessage);
					this.lockdownManager.setSystemMessageSet(true);

					save = true;
				}
			}

			if (!StringUtils.equals(this.lockdownManager.getLockdownMessage(), newLockdownMessage)) {
				this.lockdownManager.setLockdownMessage(newLockdownMessage);

				LockdownMessageListener.all().forEach(l -> l.onLockdownMessageChanged());

				save = true;
			}
		} else {
			if (this.lockdownManager.getUseSystemMessage() && this.lockdownManager.getSystemMessageSet()
					&& this.isSystemMessageUnchanged()) {
				Jenkins.get().setSystemMessage(null);

				this.lockdownManager.setLastSystemMessage(null);
				this.lockdownManager.setSystemMessageSet(false);

				save = true;
			}

			if (this.lockdownManager.getLockdownMessage() != null) {
				this.lockdownManager.setLockdownMessage(null);

				LockdownMessageListener.all().forEach(l -> l.onLockdownMessageChanged());

				save = true;
			}
		}

		if (save) {
			this.lockdownManager.save();
		}
	}

	private boolean canUpdateSystemMessage() {
		// Only set system message if it is empty, or if the current system message
		// is the same as the lockdowm message set last time. This prevents custom
		// system messages from being overwritten.
		return StringUtils.isEmpty(Jenkins.get().getSystemMessage())
				|| (this.lockdownManager.getSystemMessageSet() && this.isSystemMessageUnchanged());
	}

	private boolean isSystemMessageUnchanged() {
		return StringUtils.equals(this.lockdownManager.getLastSystemMessage(), Jenkins.get().getSystemMessage());
	}

}
