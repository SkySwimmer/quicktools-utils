package usr.skyswimmer.githubwebhooks.api.util.patterns;

import java.util.ArrayList;

public class WildcardPatternMatcher {

	private String[] pattern;
	private boolean endswithWildcard;

	public WildcardPatternMatcher(String pattern) {
		this.pattern = pattern.split("\\*");
		endswithWildcard = pattern.endsWith("*");
	}

	public PatternMatchResult match(String text) {
		boolean isFirst = true;
		ArrayList<String> params = new ArrayList<String>();
		for (int i = 0; i < pattern.length; i++) {
			String part = pattern[i];

			// First
			if (isFirst) {
				isFirst = false;

				// Check starts
				if (!text.startsWith(part)) {
					// No match
					return new PatternMatchResult(false, new String[0]);
				}

				// Substring
				String val = text.substring(part.length());

				// Check next part
				if (i + 1 < pattern.length) {
					// Increase
					i++;

					// Get part
					part = pattern[i];

					// Check
					int index = val.indexOf(part);
					if (index == -1) {
						// No match
						return new PatternMatchResult(false, new String[0]);
					}

					// Substring
					String param = val.substring(0, index);
					val = val.substring(index + part.length());
					text = val;

					// Add parameter
					params.add(param);

					// Check last
					if (i + 1 == pattern.length) {
						// Check value
						// If needed, add trailing value to parameters for when the final pattern part
						// is also wildcard
						if (!val.isEmpty())
							params.add(val);

						// Check if the pattern is a endswith wildcard
						if (!endswithWildcard && !text.isEmpty()) {
							// Invalid
							return new PatternMatchResult(false, params.toArray(t -> new String[t]));
						}
					}
				} else {
					// Update parameters
					params.add(val);

					// Check if the pattern is a endswith wildcard
					if (!endswithWildcard && !val.isEmpty()) {
						// Invalid
						return new PatternMatchResult(false, params.toArray(t -> new String[t]));
					}

					// Success
					return new PatternMatchResult(true, params.toArray(t -> new String[t]));
				}
			} else {
				// Create second variable
				String val = text;

				// Check if the next pattern part is present
				int index = val.indexOf(part);
				if (index == -1) {
					// No match
					return new PatternMatchResult(false, new String[0]);
				}

				// Substring
				String param = val.substring(0, index);
				val = val.substring(index + part.length());
				text = val;

				// Add parameter
				params.add(param);

				// Check last
				if (i + 1 == pattern.length) {
					// Check value
					// If needed, add trailing value to parameters for when the final pattern part
					// is also wildcard
					if (!val.isEmpty())
						params.add(val);

					// Check if the pattern is a endswith wildcard
					if (!endswithWildcard && !text.isEmpty()) {
						// Invalid
						return new PatternMatchResult(false, params.toArray(t -> new String[t]));
					}
				}
			}
		}

		// Success
		return new PatternMatchResult(true, params.toArray(t -> new String[t]));
	}

}
