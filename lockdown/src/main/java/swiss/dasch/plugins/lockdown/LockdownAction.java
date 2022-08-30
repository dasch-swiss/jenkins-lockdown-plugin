package swiss.dasch.plugins.lockdown;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.verb.GET;
import org.kohsuke.stapler.verb.POST;

import hudson.model.Action;
import hudson.model.Hudson;
import hudson.model.Job;
import hudson.security.Permission;
import hudson.util.HttpResponses;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

@ExportedBean(defaultVisibility = 3)
public class LockdownAction implements Action {

	private static final Logger LOGGER = Logger.getLogger(LockdownAction.class.getName());

	public static final Permission READ = LockdownPlugin.READ;
	public static final Permission START_LOCKDOWN = LockdownPlugin.START_LOCKDOWN;
	public static final Permission STOP_LOCKDOWN = LockdownPlugin.STOP_LOCKDOWN;

	private transient Job<?, ?> job;
	private transient LockdownStateProperty lockdownState;

	public LockdownAction(Job<?, ?> job) {
		this.job = job;
		this.lockdownState = job.getProperty(LockdownStateProperty.class);
		if (this.lockdownState == null) {
			try {
				job.addProperty(this.lockdownState = new LockdownStateProperty());
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, "Failed creating lockdown property.", e);
			}
		}
	}

	@Exported
	public Job<?, ?> getJob() {
		return this.job;
	}

	@Exported
	public LockdownStateProperty getLockdownState() {
		return this.lockdownState;
	}

	@GET
	public void doLockdownState(StaplerRequest req, StaplerResponse resp) throws Exception {
		Jenkins.get().checkPermission(READ);

		JSONObject json = new JSONObject();
		json.element("lockedDown", this.lockdownState.getLockedDown());
		json.elementOpt("reason", this.lockdownState.getLockdownReason());
		json.elementOpt("userId", this.lockdownState.getLockedDownByUserId());
		json.elementOpt("userName", this.lockdownState.getLockedDownByUserName());

		HttpResponses.okJSON(json).generateResponse(req, resp, this);
	}

	@POST
	public void doStartLockdown(StaplerRequest req, StaplerResponse resp) throws Exception {
		Jenkins.get().checkPermission(START_LOCKDOWN);

		String currentUserId = Hudson.getAuthentication2().getName();

		LockdownManager.get().startLockdown(this.job, currentUserId, req.getSubmittedForm().getString("reason"));

		resp.forwardToPreviousPage(req);
	}

	@POST
	public void doStopLockdown(StaplerRequest req, StaplerResponse resp) throws Exception {
		Jenkins.get().checkPermission(STOP_LOCKDOWN);

		LockdownManager.get().stopLockdown(this.job);

		resp.forwardToPreviousPage(req);
	}

	@Override
	public String getDisplayName() {
		return Messages.LockdownAction_DisplayName();
	}

	@Override
	public String getIconFileName() {
		return Jenkins.get().hasPermission(READ) ? "lock.png" : null;
	}

	@Override
	public String getUrlName() {
		return Jenkins.get().hasPermission(READ) ? "lockdown" : null;
	}

}
