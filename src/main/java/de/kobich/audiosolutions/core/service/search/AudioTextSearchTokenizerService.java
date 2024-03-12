package de.kobich.audiosolutions.core.service.search;

import java.util.List;

import org.apache.commons.text.StringTokenizer;
import org.apache.commons.text.matcher.StringMatcher;
import org.apache.commons.text.matcher.StringMatcherFactory;
import org.springframework.stereotype.Service;

import de.kobich.audiosolutions.core.service.search.AudioTextSearchToken.SearchTokenType;

@Service
public class AudioTextSearchTokenizerService {
	
	public AudioTextSearchTokens tokenize(String input) {
		List<String> words = getWords(input);
		
		AudioTextSearchTokens tokens = new AudioTextSearchTokens();
		SearchTokenType currentType = SearchTokenType.UNDEFINED;
		for (final String word : words) {
			String value = word;
			if (currentType.isUndefined()) {
				TypeAndValue typeAndValue = getTypeAndValue(word);
				currentType = typeAndValue.type;
				value = typeAndValue.value;
			}
			
			if (value.isEmpty()) {
				continue;
			}
			if (!currentType.isUndefined() && ":".equals(value)) {
				continue;
			}
			
			tokens.addToken(new AudioTextSearchToken(currentType, value));
			currentType = SearchTokenType.UNDEFINED;
		}
		return tokens;
	}
	
	private record TypeAndValue(SearchTokenType type, String value) {}
	
	/**
	 * Returns type and value of a word.
	 * Examples: 
	 * artist:stones -> type=ARTIST, VALUE=stones
	 * artist:       -> type=ARTIST, VALUE=""
	 * 
	 * @param word
	 * @return
	 */
	private TypeAndValue getTypeAndValue(String word) {
		for (SearchTokenType type : SearchTokenType.values()) {
			if (type.isUndefined()) {
				continue;
			}
			
			if (word.startsWith(type.getKeyWord())) {
				String newWord = word.substring(type.getKeyWord().length());
				if (newWord.startsWith(":")) {
					newWord = newWord.substring(":".length());
				}
				return new TypeAndValue(type, newWord);
			}
		}
		return new TypeAndValue(SearchTokenType.UNDEFINED, word);
	}
	
	private List<String> getWords(String input) {
		StringTokenizer st = new StringTokenizer(input);
		StringMatcher matcher = StringMatcherFactory.INSTANCE.quoteMatcher();
		st.setQuoteMatcher(matcher);
		return st.getTokenList();
	}
}
