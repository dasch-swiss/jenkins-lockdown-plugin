package swiss.dasch.plugins.lockdown;

import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.StaplerResponse2;
import org.kohsuke.stapler.verb.GET;

import hudson.Extension;
import hudson.model.RootAction;
import hudson.util.HttpResponses;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

@Extension
public class LockdownRootAction implements RootAction {

	@GET
	public void doLockdownMessage(StaplerRequest2 req, StaplerResponse2 resp) throws Exception {
		Jenkins.get().checkPermission(LockdownPlugin.READ);

		String message = LockdownManager.get().getFormattedLockdownMessage();

		JSONObject json = new JSONObject();
		json.element("message", message != null ? message : "");

		HttpResponses.okJSON(json).generateResponse(req, resp, this);
	}

	@Override
	public String getIconFileName() {
		return null;
	}

	@Override
	public String getDisplayName() {
		return null;
	}

	@Override
	public String getUrlName() {
		return this.hasAnyLockdownPermissions() ? "lockdown" : null;
	}

	private boolean hasAnyLockdownPermissions() {
		return Jenkins.get().hasPermission(LockdownPlugin.READ)
				|| Jenkins.get().hasPermission(LockdownPlugin.START_LOCKDOWN)
				|| Jenkins.get().hasPermission(LockdownPlugin.STOP_LOCKDOWN);
	}

}
