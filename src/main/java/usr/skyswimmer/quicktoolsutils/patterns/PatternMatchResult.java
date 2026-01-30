package usr.skyswimmer.quicktoolsutils.patterns;

public class PatternMatchResult {

	private boolean matched;
	private String[] parameters;

	public PatternMatchResult(boolean matched, String[] parameters) {
		this.matched = matched;
		this.parameters = parameters;
	}

	public boolean isMatch() {
		return matched;
	}

	public String[] getParameters() {
		return parameters;
	}

}
