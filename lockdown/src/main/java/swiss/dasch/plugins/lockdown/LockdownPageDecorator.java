package swiss.dasch.plugins.lockdown;

import hudson.Extension;
import hudson.model.PageDecorator;

@Extension
public class LockdownPageDecorator extends PageDecorator {
	public boolean hasLockdownMessage() {
		String message = LockdownManager.get().getLockdownMessage();
		return message != null && message.trim().length() > 0;
	}

	public String getLockdownMessage() {
		return LockdownManager.get().getLockdownMessage();
	}
}
