package swiss.dasch.plugins.lockdown.blueocean;

import org.jenkinsci.plugins.pubsub.MessageException;
import org.jenkinsci.plugins.pubsub.PubsubBus;
import org.jenkinsci.plugins.pubsub.SimpleMessage;

import hudson.Extension;
import hudson.model.Job;
import swiss.dasch.plugins.lockdown.LockdownMessageListener;
import swiss.dasch.plugins.lockdown.LockdownStateListener;

@Extension
public class LockdownBusDispatcher implements LockdownMessageListener, LockdownStateListener {

	public static final String CHANNEL_LOCKDOWN = "lockdown";

	public static final String EVENT_LOCKDOWN_MESSAGE_CHANGED = "lockdown_message_changed";
	public static final String EVENT_LOCKDOWN_STATE_CHANGED = "lockdown_state_changed";

	public static final String PROPERTY_LOCKDOWN_STATE = "lockdown_state";
	public static final String PROPERTY_LOCKDOWN_JOB_NAME = "lockdown_job_name";
	public static final String PROPERTY_LOCKDOWN_JOB_URL = "lockdown_job_url";

	@Override
	public void onLockdownMessageChanged() {
		try {
			PubsubBus.getBus().publish(
					new SimpleMessage().setChannelName(CHANNEL_LOCKDOWN).setEventName(EVENT_LOCKDOWN_MESSAGE_CHANGED));
		} catch (MessageException e) {
			// TODO Log?
		}
	}

	@Override
	public void onLockdownStateChanged(Job<?, ?> job, boolean lockedDown) {
		try {
			PubsubBus.getBus().publish(new SimpleMessage().setChannelName(CHANNEL_LOCKDOWN)
					.setEventName(EVENT_LOCKDOWN_STATE_CHANGED).set(PROPERTY_LOCKDOWN_STATE, String.valueOf(lockedDown))
					.set(PROPERTY_LOCKDOWN_JOB_NAME, job.getName()).set(PROPERTY_LOCKDOWN_JOB_URL, job.getUrl()));
		} catch (MessageException e) {
			// TODO Log?
		}
	}

}
