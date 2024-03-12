package de.kobich.audiosolutions.core.service.medium;

import java.util.List;

/**
 * Response of medium lend.
 * @author ckorn
 */
public class MediumResponse {
	private List<String> succeededMediumNames;
	private List<String> failedMediumNames;
	
	/**
	 * Constructor
	 * @param succeededMediumNames the succeeded mediums
	 * @param failedMediumNames the failed mediums
	 */
	public MediumResponse(List<String> succeededMediumNames, List<String> failedMediumNames) {
		this.succeededMediumNames = succeededMediumNames;
		this.failedMediumNames = failedMediumNames;
	}

	/**
	 * @return the succeededMediumNames
	 */
	public List<String> getSucceededMediumNames() {
		return succeededMediumNames;
	}

	/**
	 * @return the failedMediumNames
	 */
	public List<String> getFailedMediumNames() {
		return failedMediumNames;
	}
}
