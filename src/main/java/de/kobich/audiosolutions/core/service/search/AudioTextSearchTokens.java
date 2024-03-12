package de.kobich.audiosolutions.core.service.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.kobich.audiosolutions.core.service.search.AudioTextSearchToken.SearchTokenType;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class AudioTextSearchTokens {
	private final List<AudioTextSearchToken> tokens;
	@ToString.Exclude
	private final Set<SearchTokenType> allTypes;
	@ToString.Exclude
	private boolean allUndefined;
	
	public AudioTextSearchTokens() {
		this.tokens = new ArrayList<>();
		this.allTypes = new HashSet<>();
		this.allUndefined = true;
	}
	
	public void addToken(AudioTextSearchToken token) {
		this.tokens.add(token);
		this.allTypes.add(token.getType());
		this.allUndefined &= token.getType().isUndefined();
	}
	
	public boolean containsOnlyTypes(SearchTokenType... types) {
		Set<SearchTokenType> copy = new HashSet<>(allTypes);
		copy.removeAll(Arrays.asList(types));
		return copy.isEmpty();
	}

}
