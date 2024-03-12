package de.kobich.audiosolutions.core.service.play.player;

public class AudioPlayerResponse {
	public static enum PlayListFlowType {
		PREVIOUS_TRACK, NEXT_TRACK, TRACK_FINISHED, REPEAT_TRACK, STOP
	}

	private final long nextBeginMillis;
	private final PlayListFlowType flowType;

	/**
	 * @param flowType
	 */
	public AudioPlayerResponse(PlayListFlowType flowType) {
		this(flowType, 0);
	}
	/**
	 * @param flowType
	 * @param nextBeginMillis
	 */
	public AudioPlayerResponse(PlayListFlowType flowType, long nextBeginMillis) {
		this.flowType = flowType;
		this.nextBeginMillis = nextBeginMillis;
	}

	/**
	 * @return the nextBeginMillis
	 */
	public long getNextBeginMillis() {
		return nextBeginMillis;
	}

	/**
	 * @return the flowType
	 */
	public PlayListFlowType getFlowType() {
		return flowType;
	}

}
