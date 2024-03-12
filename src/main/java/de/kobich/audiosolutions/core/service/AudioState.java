package de.kobich.audiosolutions.core.service;

/**
 * Audio state.
 * @author ckorn
 */
public enum AudioState {
	/** not saved, some attributes are missing */
	TRANSIENT_INCOMPLETE,
	/** not saved */
	TRANSIENT,
	/** saved, some attributes are missing */
	PERSISTENT_INCOMPLETE,
	/** saved */
	PERSISTENT,
	/** saved, some attributes are missing */
	PERSISTENT_MODIFIED_INCOMPLETE,
	/** saved */
	PERSISTENT_MODIFIED,
	/** removed */
	REMOVED;

	public static AudioState getByName(String name) {
		for (AudioState state : AudioState.values()) {
			if (state.name().equals(name)) {
				return state;
			}
		}
		return null;
	}

	public boolean isTransient() {
		return AudioState.TRANSIENT.equals(this) || AudioState.TRANSIENT_INCOMPLETE.equals(this);
	}
	
	public boolean isIncomplete() {
		return AudioState.PERSISTENT_INCOMPLETE.equals(this) || AudioState.PERSISTENT_MODIFIED_INCOMPLETE.equals(this) || AudioState.TRANSIENT_INCOMPLETE.equals(this);
	}

	public boolean isPersistent() {
		boolean persistent = AudioState.PERSISTENT.equals(this) || AudioState.PERSISTENT_INCOMPLETE.equals(this);
		boolean persistentModified = isPersistentModified();
		return persistent || persistentModified;
	}
	
	public boolean isPersistentModified() {
		return AudioState.PERSISTENT_MODIFIED.equals(this) || AudioState.PERSISTENT_MODIFIED_INCOMPLETE.equals(this);
	}
	
	public AudioState nextForModified() {
		boolean complete = !this.isIncomplete();
		switch (this) {
			case PERSISTENT:
			case PERSISTENT_INCOMPLETE:
			case PERSISTENT_MODIFIED_INCOMPLETE:
			case PERSISTENT_MODIFIED:
				return complete ? PERSISTENT_MODIFIED : PERSISTENT_MODIFIED_INCOMPLETE;
			case REMOVED:
			case TRANSIENT:
			case TRANSIENT_INCOMPLETE:
			default:
				return complete ? TRANSIENT : TRANSIENT_INCOMPLETE;
		}
	}
	
	public AudioState nextForPersisted() {
		boolean complete = !this.isIncomplete();
		return complete ? PERSISTENT : PERSISTENT_INCOMPLETE;
	}
}
