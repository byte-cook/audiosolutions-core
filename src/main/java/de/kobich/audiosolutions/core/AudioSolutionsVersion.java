package de.kobich.audiosolutions.core;

import java.util.Optional;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum AudioSolutionsVersion {
	V8_0("8.0.0.0"),
	V9_0("9.0.0.0"),
	V10_0("10.0.0.0");
	
	private final String label;
	
	public static Optional<AudioSolutionsVersion> parse(String s) {
		for (AudioSolutionsVersion v : AudioSolutionsVersion.values()) {
			if (v.label.startsWith(s)) {
				return Optional.of(v);
			}
		}
		return Optional.empty();
	}
}
