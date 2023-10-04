package swiss.dasch.plugins.lockdown;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;

@ExportedBean(defaultVisibility = 4)
public class LockdownStateProperty extends JobProperty<Job<?, ?>> implements ILockdownState {

	@DataBoundConstructor
	public LockdownStateProperty() {
	}

	private ILockdownState getLockdownState() {
		return this.owner != null ? LockdownManager.get().getLockdownState(this.owner) : null;
	}

	@Exported
	@Override
	public boolean getLockedDown() {
		ILockdownState state = this.getLockdownState();
		return state != null ? state.getLockedDown() : false;
	}

	@Exported
	@Override
	public String getLockdownReason() {
		ILockdownState state = this.getLockdownState();
		return state != null ? state.getLockdownReason() : null;
	}

	@Exported
	@Override
	public String getLockedDownByUserId() {
		ILockdownState state = this.getLockdownState();
		return state != null ? state.getLockedDownByUserId() : null;
	}

	@Exported
	@Override
	public String getLockedDownByUserName() {
		ILockdownState state = this.getLockdownState();
		return state != null ? state.getLockedDownByUserName() : null;
	}

	@Extension
	public static final class DescriptorImpl extends JobPropertyDescriptor {
	}

}
