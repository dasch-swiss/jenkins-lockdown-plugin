package swiss.dasch.plugins.lockdown;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;

@ExportedBean(defaultVisibility = 4)
public class LockdownStateProperty extends JobProperty<Job<?, ?>> {

	private boolean lockedDown;
	private String lockdownReason;
	private String userId;
	private String userName;

	@DataBoundConstructor
	public LockdownStateProperty() {
	}

	@Exported
	public boolean getLockedDown() {
		return this.lockedDown;
	}

	public void setLockedDown(boolean lockedDown) {
		this.lockedDown = lockedDown;
	}

	@Exported
	public String getLockdownReason() {
		return this.lockdownReason;
	}

	public void setLockdownReason(String reason) {
		this.lockdownReason = reason;
	}

	@Exported
	public String getLockedDownByUserId() {
		return this.userId;
	}

	@Exported
	public String getLockedDownByUserName() {
		return this.userName;
	}

	public void setLockedDownByUser(String userId, String userName) {
		this.userId = userId;
		this.userName = userName;
	}

	@Extension
	public static final class DescriptorImpl extends JobPropertyDescriptor {
	}

}
