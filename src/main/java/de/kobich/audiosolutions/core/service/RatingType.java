package de.kobich.audiosolutions.core.service;

public enum RatingType {
	VERY_HIGH, HIGH, NORMAL, LOW, VERY_LOW;
	
	public static RatingType getByName(String name) {
		for (RatingType type : RatingType.values()) {
			if (type.name().equals(name)) {
				return type;
			}
		}
		return null;
	}
}
