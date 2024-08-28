package swiss.dasch.plugins.lockdown;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import hudson.AbortException;
import hudson.BulkChange;
import hudson.Extension;
import hudson.Util;
import hudson.markup.EscapedMarkupFormatter;
import hudson.markup.MarkupFormatter;
import hudson.model.Job;
import hudson.model.TopLevelItem;
import hudson.model.User;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import jenkins.model.ParameterizedJobMixIn.ParameterizedJob;
import net.sf.json.JSONObject;

@ExportedBean(defaultVisibility = 2)
@Symbol("lockdown")
@Extension
public class LockdownManager extends GlobalConfiguration {

	private static final Logger LOGGER = Logger.getLogger(LockdownManager.class.getName());

	private static final MarkupFormatter FORMATTER = new EscapedMarkupFormatter();

	private String lockdownMessageTemplate;
	private String jobMessageTemplate;
	private boolean useSystemMessage;

	// These are not manually configurable but are persisted
	private String lockdownMessage = "";
	private Map<String, LockdownState> lockdownStates = new HashMap<>();

	public LockdownManager() {
		this.resetProperties();
		this.load();
	}

	private synchronized LockdownState getOrCreateLockdownState(String job, boolean create) {
		LockdownState state = this.lockdownStates.get(job);
		if (state == null && create) {
			this.lockdownStates.put(job, state = new LockdownState());
		}
		return state;
	}

	private LockdownState getOrCreateLockdownState(Job<?, ?> job, boolean create) {
		return this.getOrCreateLockdownState(job.getFullName(), create);
	}

	public ILockdownState getLockdownState(Job<?, ?> job) {
		return this.getOrCreateLockdownState(job, false);
	}

	public synchronized boolean deleteStaleLockdownStates() {
		Jenkins jenkins = Jenkins.get();

		boolean changed = false;

		Iterator<String> it = this.lockdownStates.keySet().iterator();
		while (it.hasNext()) {
			String job = it.next();

			TopLevelItem item = jenkins.getItem(job);

			if (item instanceof Job == false) {
				it.remove();
				changed = true;
			}
		}

		if (changed) {
			this.save();
		}

		return changed;
	}

	private synchronized void resetProperties() {
		this.lockdownMessageTemplate = null;
		this.jobMessageTemplate = null;
		this.useSystemMessage = true;
	}

	public String getLockdownMessageTemplate() {
		return this.lockdownMessageTemplate;
	}

	@DataBoundSetter
	public void setLockdownMessageTemplate(String template) {
		this.lockdownMessageTemplate = template;
	}

	public String getJobMessageTemplate() {
		return this.jobMessageTemplate;
	}

	@DataBoundSetter
	public void setJobMessageTemplate(String template) {
		this.jobMessageTemplate = template;
	}

	public boolean getUseSystemMessage() {
		return this.useSystemMessage;
	}

	@DataBoundSetter
	public void setUseSystemMessage(boolean use) {
		this.useSystemMessage = use;
	}

	@Exported
	public String getLockdownMessage() {
		return this.lockdownMessage;
	}

	@Exported
	public String getFormattedLockdownMessage() {
		try {
			return this.lockdownMessage == null ? null
					: Jenkins.get().getMarkupFormatter().translate(this.lockdownMessage);
		} catch (IOException | IllegalStateException e) {
			return null;
		}
	}

	public void setLockdownMessage(String message) {
		this.lockdownMessage = message;
	}

	public boolean startLockdown(Job<?, ?> job, String userId, String reason) {
		return this.startLockdown(job, userId, null, reason);
	}

	public synchronized boolean startLockdown(Job<?, ?> job, String userId, String usernameOverride, String reason) {
		ParameterizedJob<?, ?> paramJob = job instanceof ParameterizedJob ? (ParameterizedJob<?, ?>) job : null;

		LockdownState state = this.getOrCreateLockdownState(job, true);

		if (!state.getLockedDown() && (paramJob == null || !paramJob.isDisabled())
				&& this.isLockdownPluginEnabled(job)) {
			User user = User.get(userId, false, Collections.emptyMap());
			String userName = usernameOverride != null ? usernameOverride
					: user != null ? user.getDisplayName() : userId;

			if (paramJob != null) {
				try {
					paramJob.makeDisabled(true);
				} catch (IOException e) {
					LOGGER.log(Level.SEVERE, "Failed starting lockdown because job could not be disabled.", e);
				}
			}

			state.setLockedDown(true);
			state.setLockdownReason(reason);
			state.setLockedDownByUser(userId, userName);

			this.save();

			LockdownStateListener.all().forEach(l -> l.onLockdownStateChanged(job, true));

			return true;
		}

		return false;
	}

	public boolean stopLockdown(Job<?, ?> job) {
		return this.stopLockdown(job, true);
	}

	public synchronized boolean stopLockdown(Job<?, ?> job, boolean checkForPluginEnabled) {
		ParameterizedJob<?, ?> paramJob = job instanceof ParameterizedJob ? (ParameterizedJob<?, ?>) job : null;

		LockdownState state = this.getOrCreateLockdownState(job, false);

		if (state != null && state.getLockedDown() && (!checkForPluginEnabled || this.isLockdownPluginEnabled(job))) {
			state.setLockedDown(false);

			if (paramJob != null) {
				try {
					paramJob.makeDisabled(false);
				} catch (IOException e) {
					LOGGER.log(Level.SEVERE, "Failed stopping lockdown because job could not be enabled.", e);
				}
			}

			this.save();

			LockdownStateListener.all().forEach(l -> l.onLockdownStateChanged(job, false));

			return true;
		}

		return false;
	}

	public boolean performLockdownStep(Job<?, ?> job, LockdownStepConfig config) throws AbortException {
		if (!LockdownManager.get().isLockdownPluginEnabled(job)) {
			throw new AbortException(
					"Cannot start or stop lockdown because the lockdown plugin is not enabled for this job.");
		}

		boolean isLockedDown = LockdownManager.get().isLockedDown(job);

		boolean isJobDisabled = job instanceof ParameterizedJob ? ((ParameterizedJob<?, ?>) job).isDisabled() : false;

		boolean changed = false;

		if (config.getStart()) {
			changed = LockdownManager.get().startLockdown(job, Jenkins.ANONYMOUS2.getName(), "LockdownStep",
					config.getReason() != null ? config.getReason() : "");

			if (!changed && config.getAbortOnFail()) {
				throw new AbortException(
						"Failed starting lockdown" + (isLockedDown ? " because this job is already locked down."
								: isJobDisabled ? " because this job is already disabled." : "."));
			}
		} else {
			changed = LockdownManager.get().stopLockdown(job);

			if (!changed && config.getAbortOnFail()) {
				throw new AbortException("Failed stopping lockdown"
						+ (!isLockedDown ? " because this job is not yet locked down." : "."));
			}
		}

		return changed;
	}

	public void ensureDisabledState(Job<?, ?> job) {
		if (job instanceof ParameterizedJob) {
			ParameterizedJob<?, ?> paramJob = (ParameterizedJob<?, ?>) job;

			if (!paramJob.isDisabled()) {
				ILockdownState state = this.getLockdownState(job);

				if (state != null && state.getLockedDown()) {
					LOGGER.log(Level.INFO, "Disabling locked down job.");
					try {
						paramJob.makeDisabled(true);
					} catch (IOException e) {
						LOGGER.log(Level.SEVERE, "Failed disabling locked down job.", e);
					}
				}
			}
		}
	}

	public synchronized boolean isLockedDown(Job<?, ?> job) {
		ILockdownState state = this.getLockdownState(job);
		return state != null && state.getLockedDown() && this.isLockdownPluginEnabled(job);
	}

	public boolean isLockdownPluginEnabled(Job<?, ?> job) {
		LockdownJobProperty property = job.getProperty(LockdownJobProperty.class);
		return property != null ? property.getEnabled() : false;
	}

	@Exported
	public boolean hasLockdowns() {
		return Util.createSubList(Jenkins.get().getItems(), Job.class).stream()
				.map(p -> p.getAction(LockdownAction.class)).filter(a -> a != null
						&& a.getLockdownState().getLockedDown() && this.isLockdownPluginEnabled(a.getJob()))
				.findAny().isPresent();
	}

	@Exported
	public List<LockdownAction> getLockdowns() {
		return Util.createSubList(Jenkins.get().getItems(), Job.class).stream()
				.map(p -> p.getAction(LockdownAction.class)).filter(a -> a != null
						&& a.getLockdownState().getLockedDown() && this.isLockdownPluginEnabled(a.getJob()))
				.collect(Collectors.toList());
	}

	public String renderLockdownMessage() throws IOException {
		if (this.lockdownMessageTemplate != null) {
			List<LockdownAction> lockdowns = this.getLockdowns();

			if (!lockdowns.isEmpty()) {
				StringBuilder jobMessage = new StringBuilder();

				for (LockdownAction lockdown : lockdowns) {
					jobMessage.append(this.renderJobMessage(lockdown.getJob(), lockdown));
				}

				return this.lockdownMessageTemplate.replaceAll(getPattern("jobs"), jobMessage.toString());
			}
		}
		return "";
	}

	public String renderJobMessage(Job<?, ?> job, LockdownAction lockdown) throws IOException {
		if (this.jobMessageTemplate != null) {
			return this.jobMessageTemplate.replaceAll(getPattern("name"), sanitizeText(job.getDisplayName()))
					.replaceAll(getPattern("fullname"), sanitizeText(job.getFullName()))
					.replaceAll(getPattern("description"), sanitizeText(job.getDescription()))
					.replaceAll(getPattern("url"), job.getAbsoluteUrl())
					.replaceAll(getPattern("reason"), sanitizeText(lockdown.getLockdownState().getLockdownReason()))
					.replaceAll(getPattern("username"),
							sanitizeText(lockdown.getLockdownState().getLockedDownByUserName()))
					.replaceAll(getPattern("userid"),
							sanitizeText(lockdown.getLockdownState().getLockedDownByUserId()));
		}
		return "";
	}

	private static String sanitizeText(String text) throws IOException {
		return text != null ? FORMATTER.translate(text) : null;
	}

	private static String getPattern(String key) {
		return String.format("\\{%%\\s*%s\\s*%%\\}", key);
	}

	@Override
	public boolean configure(StaplerRequest req, JSONObject json) {
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

	public static LockdownManager get() {
		return (LockdownManager) Jenkins.get().getDescriptorOrDie(LockdownManager.class);
	}

}
