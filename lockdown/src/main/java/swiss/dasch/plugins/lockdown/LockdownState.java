package swiss.dasch.plugins.lockdown;

import java.io.Serializable;

public class LockdownState implements ILockdownState, Serializable {

	private static final long serialVersionUID = 2004896243656928711L;

	private boolean lockedDown;
	private String lockdownReason;
	private String userId;
	private String userName;

	@Override
	public boolean getLockedDown() {
		return this.lockedDown;
	}

	public void setLockedDown(boolean lockedDown) {
		this.lockedDown = lockedDown;
	}

	@Override
	public String getLockdownReason() {
		return this.lockdownReason;
	}

	public void setLockdownReason(String reason) {
		this.lockdownReason = reason;
	}

	@Override
	public String getLockedDownByUserId() {
		return this.userId;
	}

	@Override
	public String getLockedDownByUserName() {
		return this.userName;
	}

	public void setLockedDownByUser(String userId, String userName) {
		this.userId = userId;
		this.userName = userName;
	}

}
