package swiss.dasch.plugins.lockdown;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import hudson.BulkChange;
import hudson.Extension;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import net.sf.json.JSONObject;

@ExportedBean
public class LockdownJobProperty extends JobProperty<Job<?, ?>> {

	private boolean enabled;

	@DataBoundConstructor
	public LockdownJobProperty(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	protected void setOwner(Job<?, ?> owner) {
		super.setOwner(owner);

		if (owner != null && !this.enabled) {
			LockdownManager.get().stopLockdown(owner, false);
		}
	}

	@Exported(inline = true)
	public boolean getEnabled() {
		return this.enabled;
	}

	@Symbol("lockdown")
	@Extension
	public static final class DescriptorImpl extends JobPropertyDescriptor {
		private static final Logger LOGGER = Logger.getLogger(DescriptorImpl.class.getName());

		private boolean enabled;

		@DataBoundConstructor
		public DescriptorImpl() {
			this.resetProperties();
			this.load();
		}

		private void resetProperties() {
			this.enabled = false;
		}

		public boolean getEnabled() {
			return this.enabled;
		}

		@DataBoundSetter
		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
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

		@Override
		public String getDisplayName() {
			return Messages.LockdownJobProperty_DisplayName();
		}
	}

}
