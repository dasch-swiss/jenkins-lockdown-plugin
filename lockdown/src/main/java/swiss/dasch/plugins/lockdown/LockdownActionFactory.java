package swiss.dasch.plugins.lockdown;

import java.util.Collection;
import java.util.Collections;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.Job;
import jenkins.model.TransientActionFactory;

@Extension
public class LockdownActionFactory extends TransientActionFactory<Job<?, ?>> {

	@Override
	public Collection<? extends Action> createFor(Job<?, ?> job) {
		LockdownJobProperty lockdownProperty = (LockdownJobProperty) job.getProperty(LockdownJobProperty.class);
		if (lockdownProperty != null && lockdownProperty.getEnabled()) {
			return Collections.singleton(new LockdownAction(job));
		} else {
			return Collections.emptyList();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<Job<?, ?>> type() {
		return (Class<Job<?, ?>>) (Class<?>) Job.class;
	}

}
