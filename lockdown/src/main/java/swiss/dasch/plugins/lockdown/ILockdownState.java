package swiss.dasch.plugins.lockdown;

public interface ILockdownState {

	boolean getLockedDown();

	String getLockdownReason();

	String getLockedDownByUserId();

	String getLockedDownByUserName();

}
