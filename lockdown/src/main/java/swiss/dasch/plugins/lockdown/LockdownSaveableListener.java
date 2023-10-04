package swiss.dasch.plugins.lockdown;

import hudson.Extension;
import hudson.XmlFile;
import hudson.model.Job;
import hudson.model.Saveable;
import hudson.model.listeners.SaveableListener;

@Extension
public class LockdownSaveableListener extends SaveableListener {

	private transient LockdownManager lockdownManager;

	public LockdownSaveableListener() {
		this.lockdownManager = LockdownManager.get();
	}

	@Override
	public void onChange(Saveable o, XmlFile file) {
		if (o instanceof Job) {
			this.lockdownManager.ensureDisabledState((Job<?, ?>) o);
		}
	}

}
