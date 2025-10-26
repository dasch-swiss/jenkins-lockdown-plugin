package swiss.dasch.plugins.lockdown;

import hudson.Extension;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.listeners.ItemListener;
import jenkins.model.Jenkins;

@Extension
public class LockdownItemListener extends ItemListener {

	@Override
	public void onCreated(Item item) {
		if (item instanceof Job) {
			LockdownManager.get().ensureDisabledState((Job<?, ?>) item);
		}
	}

	@Override
	public void onUpdated(Item item) {
		if (item instanceof Job) {
			LockdownManager.get().ensureDisabledState((Job<?, ?>) item);
		}
	}

	@Override
	public void onLoaded() {
		Jenkins.get().allItems(Job.class).forEach(job -> LockdownManager.get().ensureDisabledState(job));
	}

}
