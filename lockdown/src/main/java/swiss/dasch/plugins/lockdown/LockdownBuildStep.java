package swiss.dasch.plugins.lockdown;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

import hudson.BulkChange;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import net.sf.json.JSONObject;

public class LockdownBuildStep extends Builder {

	private final LockdownStepConfig config;

	@DataBoundConstructor
	public LockdownBuildStep(LockdownStepConfig config) {
		this.config = config;
	}

	public LockdownStepConfig getConfig() {
		return this.config;
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
			throws InterruptedException, IOException {

		LockdownManager.get().performLockdownStep(build.getParent(), this.getConfig());

		return true;
	}

	@Symbol("lockdown")
	@Extension
	public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
		private static final Logger LOGGER = Logger.getLogger(DescriptorImpl.class.getName());

		private LockdownStepConfig config;

		public DescriptorImpl() {
			this.resetProperties();
			this.load();
		}

		private void resetProperties() {
			this.config = new LockdownStepConfig(false);
		}

		@DataBoundSetter
		public void setConfig(LockdownStepConfig config) {
			this.config = config;
		}

		public LockdownStepConfig getConfig() {
			return this.config;
		}

		@Override
		public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
			this.resetProperties();

			try (BulkChange bc = new BulkChange(this)) {
				req.bindJSON(this, json);
				bc.commit();
			} catch (IOException ex) {
				LOGGER.log(Level.WARNING, "Exception during BulkChange.", ex);
				return false;
			}

			return true;
		}

		@SuppressWarnings("rawtypes")
		@Override
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return true;
		}

		@Override
		public String getDisplayName() {
			return Messages.LockdownBuildStep_DisplayName();
		}

	}

}
