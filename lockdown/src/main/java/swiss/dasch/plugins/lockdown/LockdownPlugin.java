package swiss.dasch.plugins.lockdown;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import hudson.Plugin;
import hudson.model.Api;
import hudson.security.Permission;
import hudson.security.PermissionGroup;
import hudson.security.PermissionScope;
import jenkins.model.Jenkins;

@SuppressWarnings("deprecation")
@ExportedBean
public class LockdownPlugin extends Plugin {

	public static final PermissionGroup PERMISSIONS_GROUP = new PermissionGroup(LockdownPlugin.class,
			Messages._LockdownPlugin_PermissionGroup());

	public static final Permission READ = new Permission(PERMISSIONS_GROUP, "Read",
			Messages._LockdownPlugin_ReadPermission_Description(), Permission.READ, PermissionScope.JENKINS);

	public static final Permission START_LOCKDOWN = new Permission(PERMISSIONS_GROUP, "Start",
			Messages._LockdownPlugin_StartLockdownPermission_Description(), Jenkins.ADMINISTER,
			PermissionScope.JENKINS);

	public static final Permission STOP_LOCKDOWN = new Permission(PERMISSIONS_GROUP, "Stop",
			Messages._LockdownPlugin_StopLockdownPermission_Description(), Jenkins.ADMINISTER, PermissionScope.JENKINS);

	public Api getApi() {
		return new Api(this);
	}

	@Exported
	public LockdownManager getLockdownManager() {
		return LockdownManager.get();
	}

}
