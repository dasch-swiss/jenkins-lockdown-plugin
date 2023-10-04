package swiss.dasch.plugins.lockdown;

import hudson.Extension;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.listeners.ItemListener;
import jenkins.model.Jenkins;

@Extension
public class LockdownItemListener extends ItemListener {

	private transient LockdownManager lockdownManager;

	public LockdownItemListener() {
		this.lockdownManager = LockdownManager.get();
	}

	@Override
	public void onCreated(Item item) {
		if (item instanceof Job) {
			this.lockdownManager.ensureDisabledState((Job<?, ?>) item);
		}
	}

	@Override
	public void onUpdated(Item item) {
		if (item instanceof Job) {
			this.lockdownManager.ensureDisabledState((Job<?, ?>) item);
		}
	}

	@Override
	public void onLoaded() {
		Jenkins.get().allItems(Job.class).forEach(job -> this.lockdownManager.ensureDisabledState(job));
	}

}
