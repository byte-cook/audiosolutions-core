package de.kobich.audiosolutions.core;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum AudioSolutionsStatus {
	NOT_WRITABLE(true), VERSION_MISMATCH(false), LOCKED(true), INITIALIZED(false);
	
	private final boolean forceExit;
}
