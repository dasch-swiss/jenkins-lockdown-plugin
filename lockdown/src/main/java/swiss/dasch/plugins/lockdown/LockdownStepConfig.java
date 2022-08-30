package swiss.dasch.plugins.lockdown;

import java.io.Serializable;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

public class LockdownStepConfig extends AbstractDescribableImpl<LockdownStepConfig> implements Serializable {

	private static final long serialVersionUID = -8231400573018938568L;

	private boolean stop;
	private String reason;

	private boolean abortOnFail;

	@DataBoundConstructor
	public LockdownStepConfig(boolean stop) {
		this.stop = stop;
	}

	@DataBoundSetter
	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getReason() {
		return this.reason;
	}

	@DataBoundSetter
	public void setStop(boolean stop) {
		this.stop = stop;
	}

	public boolean getStart() {
		return !this.stop;
	}

	public boolean getStop() {
		return this.stop;
	}

	@DataBoundSetter
	public void setAbortOnFail(boolean abortOnFail) {
		this.abortOnFail = abortOnFail;
	}

	public boolean getAbortOnFail() {
		return this.abortOnFail;
	}

	@Extension
	public static final class DescriptorImpl extends Descriptor<LockdownStepConfig> {
	}

}
