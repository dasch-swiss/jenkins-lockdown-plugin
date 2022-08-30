package swiss.dasch.plugins.lockdown;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.workflow.steps.AbstractStepExecutionImpl;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;

public class LockdownPipelineStep extends Step implements Serializable {

	private static final long serialVersionUID = 4066545670881547849L;

	// Config is only used internally here so that the
	// parameters can be set directly and individually
	// from the pipeline DSL
	private final LockdownStepConfig config;

	@DataBoundConstructor
	public LockdownPipelineStep(boolean stop) {
		this.config = new LockdownStepConfig(stop);
	}

	@DataBoundSetter
	public void setReason(String reason) {
		this.config.setReason(reason);
	}

	public String getReason() {
		return this.config.getReason();
	}

	@DataBoundSetter
	public void setStop(boolean stop) {
		this.config.setStop(stop);
	}

	public boolean getStart() {
		return this.config.getStart();
	}

	public boolean getStop() {
		return this.config.getStop();
	}

	@DataBoundSetter
	public void setAbortOnFail(boolean abortOnFail) {
		this.config.setAbortOnFail(abortOnFail);
	}

	public boolean getAbortOnFail() {
		return this.config.getAbortOnFail();
	}

	public LockdownStepConfig getConfig() {
		return this.config;
	}

	@Override
	public StepExecution start(StepContext context) throws Exception {
		return new Execution(context, this);
	}

	@Symbol("lockdown")
	@Extension
	public static final class DescriptorImpl extends StepDescriptor {

		@Override
		public Set<? extends Class<?>> getRequiredContext() {
			return Collections.singleton(Run.class);
		}

		@Override
		public String getFunctionName() {
			return "lockdown";
		}

		@Override
		public String getDisplayName() {
			return Messages.LockdownPipelineStep_DisplayName();
		}

	}

	public static class Execution extends AbstractStepExecutionImpl implements Serializable {

		private static final long serialVersionUID = -6506463934124035013L;

		private final LockdownPipelineStep step;

		public Execution(StepContext context, LockdownPipelineStep step) {
			super(context);
			this.step = step;
		}

		@Override
		public boolean start() throws Exception {
			Job<?, ?> job = this.getContext().get(Run.class).getParent();

			LockdownManager.get().performLockdownStep(job, step.getConfig());

			this.getContext().onSuccess(null);

			return true;
		}

	}

}
